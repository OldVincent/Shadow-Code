#nullable enable
using System;
using System.Data.SqlClient;
using System.IO;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.AspNetCore.Http;
using Microsoft.Azure.WebJobs.Host;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;

namespace ShadowCode;

public static class UserLogout
{
    public class Result
    {
        public int Code = CodeInnerFailure;
    }

    public const int CodeSuccess = 0;
    public const int CodeInnerFailure = -1;
    
    [FunctionName("user_logout")]
    public static async Task<IActionResult> RunAsync(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = null)] HttpRequest req, ILogger log)
    {
        dynamic data = JsonConvert.DeserializeObject(await new StreamReader(req.Body).ReadToEndAsync());

        int? id = data?.id;
        string? device = data?.device;

        if (id == null || device == null)
        {
            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeInnerFailure
            }));
        }
        
        SqlConnectionStringBuilder builder = new SqlConnectionStringBuilder();

        builder.DataSource = "shadowcode.database.windows.net"; 
        builder.UserID = "vincent";            
        builder.Password = "Jiahaoyu2000";     
        builder.InitialCatalog = "shadowcode";

        try
        {
            await using var connection = new SqlConnection(builder.ConnectionString);
            connection.Open();

            await new SqlCommand(
                $"IF EXISTS (SELECT * FROM devices WHERE user_id={id} AND device_id='{device}') "+
                $"UPDATE devices SET logout_time=GETDATE(), device_using=0 WHERE user_id={id} AND device_id='{device}'",
                connection).ExecuteNonQueryAsync();

            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeSuccess
            }));
        }
        catch (Exception exception)
        {
            log.Log(LogLevel.Critical, exception.Message);
            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeInnerFailure
            }));
        }
    }
}