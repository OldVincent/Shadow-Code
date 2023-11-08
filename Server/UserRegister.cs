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

public static class UserRegister
{
    public class Result
    {
        public int Code = CodeInnerFailure;
    }

    public const int CodeSuccess = 0;
    public const int CodeConflicting = -1;
    public const int CodeInnerFailure = -2;
    
    [FunctionName("user_register")]
    public static async Task<IActionResult> RunAsync(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = null)] HttpRequest req, ILogger log)
    {
        dynamic data = JsonConvert.DeserializeObject(await new StreamReader(req.Body).ReadToEndAsync());
        string name = data?.name;
        string password = data?.password;
        string identity = data?.identity;

        SqlConnectionStringBuilder builder = new SqlConnectionStringBuilder();

        builder.DataSource = "shadowcode.database.windows.net"; 
        builder.UserID = "vincent";            
        builder.Password = "Jiahaoyu2000";     
        builder.InitialCatalog = "shadowcode";

        try
        {
            await using SqlConnection connection = new SqlConnection(builder.ConnectionString);
            connection.Open();

            await using var command = new SqlCommand(
                "SELECT user_name, user_identity FROM users " +
                $"WHERE user_name='{name}' OR user_identity='{identity}'",
                connection);
            await using var reader = await command.ExecuteReaderAsync();
            if (reader.Read())
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeConflicting
                }));
            await reader.CloseAsync();
            await new SqlCommand(
                    "INSERT INTO users (user_name, user_password, user_identity) VALUES" +
                    $"('{name}', '{password}', '{identity}')", connection)
                .ExecuteNonQueryAsync();
            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeSuccess,
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