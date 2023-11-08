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

public static class UpdateRisk
{
    public class Result
    {
        public int Code = CodeInnerFailure;
        public int Risk;
    }
    
    public const int CodeSuccess = 0;
    public const int CodeInvalidId = -1;
    public const int CodeInnerFailure = -2;
    
    [FunctionName("update_risk")]
    public static async Task<IActionResult> RunAsync(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = null)] HttpRequest req, ILogger log)
    {
        dynamic data = JsonConvert.DeserializeObject(await new StreamReader(req.Body).ReadToEndAsync());

        int? id = data?.id;
        int? uploadingRisk = data?.risk;

        if (id == null || uploadingRisk == null)
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

            int? storedRisk = null;
            int commandId = 0;
            
            var reader = await new SqlCommand(
                $"SELECT user_risk, command_id FROM risks WHERE user_id={id}",
                connection).ExecuteReaderAsync();
            if (reader.Read())
            {
                storedRisk = reader.GetInt32(0);
                commandId = reader.GetInt32(1);
            }
            await reader.CloseAsync();

            if (storedRisk != null)
            {
                if (commandId == 1)
                {
                    await new SqlCommand(
                            $"DELETE risks WHERE user_id={id} ",
                            connection)
                        .ExecuteNonQueryAsync();
                    return new JsonResult(JsonConvert.SerializeObject(new Result()
                    {
                        Code = CodeSuccess,
                        Risk = storedRisk.Value
                    }));
                }
                if (storedRisk < uploadingRisk)
                {
                    await new SqlCommand(
                            $"UPDATE risks SET user_risk={uploadingRisk}, marking_time=GETDATE() WHERE user_id={id} ",
                            connection)
                        .ExecuteNonQueryAsync();
                    return new JsonResult(JsonConvert.SerializeObject(new Result()
                    {
                        Code = CodeSuccess,
                        Risk = uploadingRisk.Value
                    }));
                }
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeSuccess,
                    Risk = storedRisk.Value
                }));
            }
            
            if (uploadingRisk != 0 && uploadingRisk != 1)
            {
                await new SqlCommand(
                        $" INSERT INTO risks (user_id, user_risk, marking_time) VALUES ({id}, {uploadingRisk}, GETDATE()) ",
                        connection)
                    .ExecuteNonQueryAsync();
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeSuccess,
                    Risk = uploadingRisk.Value
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