package com.storyous.apitools

import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import retrofit2.HttpException
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class ApiProviderTest {

    val api = ApiProvider.getApi(TestApi::class)

    @get:Rule
    val mockServerRule = MockServerRule {
        api().baseUrl = it
        api().rebuild()
    }

    @Test
    fun testSuccessApiRequest() = runBlocking {
        mockServerRule.server.enqueue(
            MockResponse().setBody(api().gson.toJson(DataResponse("data")))
        )

        val response = api().service.callTestEndpoint()

        Assert.assertTrue(response.data == "data")
    }

    @Test
    fun testErrorApiRequest() = runBlocking {
        mockServerRule.server.enqueue(
            MockResponse().setResponseCode(404).setBody(api().gson.toJson(ErrorResponse(404)))
        )

        val errorResponse = runCatching { api().service.callTestEndpoint() }.exceptionOrNull()
            .let { (it as HttpException).response()?.errorBody() }
            ?.let { api().errorConverter.convert(it) }

        Assert.assertTrue(errorResponse?.code == 404)
    }

    @Test
    fun testAuthApiRequest() = runBlocking {
        mockServerRule.server.enqueue(
            MockResponse().setBody(api().gson.toJson(DataResponse("data")))
        )

        api().authInterceptor.authHeaderProvider = object : AuthHeaderProvider {
            override fun getAuth(): String {
                return "authHeaderValue"
            }
        }
        api().service.callTestEndpoint()

        Assert.assertEquals(
            "authHeaderValue",
            mockServerRule.server.takeRequest().headers.get(AuthInterceptor.HEADER_AUTH)
        )
    }

    @Test
    fun testHeaderApiRequest() = runBlocking {
        mockServerRule.server.enqueue(
            MockResponse().setBody(api().gson.toJson(DataResponse("data")))
        )

        api().headerInterceptor.customHeaders["testHeader"] = "testHeaderValue"
        api().service.callTestEndpoint()

        Assert.assertEquals(
            "testHeaderValue",
            mockServerRule.server.takeRequest().headers.get("testHeader")
        )
    }
}

data class DataResponse(
    val data: String
)

data class ErrorResponse(
    val code: Int
)

interface TestService {
    @GET("/test")
    suspend fun callTestEndpoint(): DataResponse
}

class TestApi : Api<TestService, ErrorResponse>() {
    val gson = GsonBuilder().create()
    val authInterceptor = AuthInterceptor()
    val headerInterceptor = HeaderInterceptor()

    override val serviceClazz = TestService::class
    override val errorClazz = ErrorResponse::class
    override var baseUrl = LOCALHOST

    override val convertFactories = listOf(GsonConverterFactory.create(gson))
    override val interceptors = listOf(authInterceptor, headerInterceptor)
}
