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

public static class QueryRiskById
{
    public class Result
    {
        public int Code = CodeInnerFailure;
        public int Risk;
    }
    
    public const int CodeSuccess = 0;
    public const int CodeInvalidId = -1;
    public const int CodeInnerFailure = -2;
    
    [FunctionName("query_risk_by_id")]
    public static async Task<IActionResult> RunAsync(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = null)] HttpRequest req, ILogger log)
    {
        dynamic data = JsonConvert.DeserializeObject(await new StreamReader(req.Body).ReadToEndAsync());

        int? id = data?.id;

        if (id == null)
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

            var reader = await new SqlCommand(
                $"SELECT user_risk FROM risks WHERE user_id={id}",
                connection).ExecuteReaderAsync();

            if (reader.Read())
            {
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeSuccess,
                    Risk = reader.GetInt32(0)
                }));
            }
            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeSuccess,
                Risk = 1
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