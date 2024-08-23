import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory

// Data classes to map the API response
data class Question(
    val title: String,
    val link: String,
    val score: Int
)

data class StackExchangeResponse(
    val items: List<Question>
)

// Retrofit interface
interface StackExchangeApiService {
    @GET("questions?order=desc&sort=activity&site=stackoverflow")
    fun getQuestions(
        @Query("tagged") tags: String,
        @Query("pagesize") pageSize: Int
    ): Call<StackExchangeResponse>
}

// Main plugin class implementing FlutterPlugin and MethodCallHandler
class StackExchangePlugin : FlutterPlugin, MethodChannel.MethodCallHandler {
    private lateinit var channel: MethodChannel

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "stack_exchange_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method == "getQuestions") {
            val tags = call.argument<String>("tags") ?: ""
            val pageSize = call.argument<Int>("pageSize") ?: 10

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.stackexchange.com/2.3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service = retrofit.create(StackExchangeApiService::class.java)
            val callApi = service.getQuestions(tags, pageSize)

            callApi.enqueue(object : Callback<StackExchangeResponse> {
                override fun onResponse(call: Call<StackExchangeResponse>, response: Response<StackExchangeResponse>) {
                    if (response.isSuccessful) {
                        val questions = response.body()?.items?.map {
                            mapOf("title" to it.title, "link" to it.link, "score" to it.score)
                        }
                        result.success(questions)
                    } else {
                        result.error("API_ERROR", "Failed to fetch data", null)
                    }
                }

                override fun onFailure(call: Call<StackExchangeResponse>, t: Throwable) {
                    result.error("NETWORK_ERROR", t.message, null)
                }
            })
        } else {
            result.notImplemented()
        }
    }
}
