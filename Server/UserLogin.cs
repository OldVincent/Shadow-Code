#nullable enable
using System;
using System.Data.SqlClient;
using System.IO;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Azure.WebJobs;
using Microsoft.Azure.WebJobs.Extensions.Http;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;
using Newtonsoft.Json;

namespace ShadowCode;

public class Result
{
    public int Code = -1;
    public int Id = 0;
    public string Identity = "";
}

public static class UserLogin
{
    public const int CodeSuccess = 0;
    public const int CodeInvalidUser = -1;
    public const int CodeInvalidPassword = -2;
    public const int CodeInnerFailure = -3;
    
    [FunctionName("user_login")]
    public static async Task<IActionResult> RunAsync(
        [HttpTrigger(AuthorizationLevel.Anonymous, "get", "post", Route = null)] HttpRequest req, ILogger log)
    {
        dynamic data = JsonConvert.DeserializeObject(await new StreamReader(req.Body).ReadToEndAsync());

        string? name = data?.name;
        string? password = data?.password;
        string? device = data?.device;

        if (name == null || password == null)
            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = -1
            }));

        SqlConnectionStringBuilder builder = new SqlConnectionStringBuilder();

        builder.DataSource = "shadowcode.database.windows.net"; 
        builder.UserID = "vincent";            
        builder.Password = "Jiahaoyu2000";     
        builder.InitialCatalog = "shadowcode";

        try
        {
            await using var connection = new SqlConnection(builder.ConnectionString);
            connection.Open();

            await using var command = new SqlCommand(
                $"SELECT user_id, user_name, user_identity, user_password FROM users WHERE user_name='{name}'",
                connection);
            await using var userReader = await command.ExecuteReaderAsync();
            if (!userReader.Read())
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeInvalidUser
                }));
            if (userReader.GetString(3) != password)
                return new JsonResult(JsonConvert.SerializeObject(new Result()
                {
                    Code = CodeInvalidPassword
                }));
            var identity = userReader.GetString(2);
            if (identity.Length < 3)
            {
                identity = identity[0] + "***";
            } else if (identity.Length < 9)
            {
                identity = identity[0] + "***" + identity[^1];
            }
            else
            {
                identity = identity[..3] + "***" + identity[^4..^0];
            }

            var id = userReader.GetInt32(0);
            await userReader.CloseAsync();
            await new SqlCommand(
                    $"IF EXISTS (SELECT * FROM devices WHERE user_id={id} AND device_id='{device}') " +
                    $"UPDATE devices SET login_time=GETDATE(), device_using=1 WHERE user_id={id} AND device_id='{device}' " +
                    $"ELSE INSERT INTO devices (user_id, device_id, device_using, login_time) VALUES ({id}, '{device}', 1, GETDATE())", connection)
                .ExecuteNonQueryAsync();

            return new JsonResult(JsonConvert.SerializeObject(new Result()
            {
                Code = CodeSuccess,
                Id = id,
                Identity = identity
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