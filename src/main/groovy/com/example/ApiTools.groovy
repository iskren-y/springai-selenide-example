package com.example

import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpTimeoutException
import java.time.Duration

@Component
class ApiTools {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()

    @Tool(description = "Execute HTTP GET request to retrieve data from a URL.")
    String httpGet(
            @ToolParam(description = "Full URL to fetch (e.g., https://api.example.com/users)") String url,
            @ToolParam(description = "Optional HTTP headers in format: header1=value1&header2=value2") String headers,
            @ToolParam(description = "Optional query parameters in format: param1=value1&param2=value2") String queryParams) {

        println("→ HTTP GET: $url")

        String fullUrl = buildUrl(url, queryParams)
        def requestBuilder = HttpRequest.newBuilder(URI.create(fullUrl))
                .GET()
                .timeout(Duration.ofSeconds(30))

        applyHeaders(requestBuilder, headers)

        return executeRequest(requestBuilder.build())
    }

    @Tool(description = "Execute HTTP POST request to send data to a URL.")
    String httpPost(
            @ToolParam(description = "Full URL to post to (e.g., https://api.example.com/users)") String url,
            @ToolParam(description = "Request body content (JSON, text, form data, etc.)") String body,
            @ToolParam(description = "Optional HTTP headers in format: header1=value1&header2=value2") String headers,
            @ToolParam(description = "Optional query parameters in format: param1=value1&param2=value2") String queryParams) {

        println("→ HTTP POST: $url")

        String fullUrl = buildUrl(url, queryParams)
        def requestBuilder = HttpRequest.newBuilder(URI.create(fullUrl))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))

        applyHeaders(requestBuilder, headers)

        return executeRequest(requestBuilder.build())
    }

    @Tool(description = "Execute HTTP PUT request to update data at a URL.")
    String httpPut(
            @ToolParam(description = "Full URL to update (e.g., https://api.example.com/users/123)") String url,
            @ToolParam(description = "Request body content (JSON, text, form data, etc.)") String body,
            @ToolParam(description = "Optional HTTP headers in format: header1=value1&header2=value2") String headers,
            @ToolParam(description = "Optional query parameters in format: param1=value1&param2=value2") String queryParams) {

        println("→ HTTP PUT: $url")

        String fullUrl = buildUrl(url, queryParams)
        def requestBuilder = HttpRequest.newBuilder(URI.create(fullUrl))
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofSeconds(30))

        applyHeaders(requestBuilder, headers)

        return executeRequest(requestBuilder.build())
    }

    @Tool(description = "Execute HTTP DELETE request to remove data at a URL.")
    String httpDelete(
            @ToolParam(description = "Full URL to delete (e.g., https://api.example.com/users/123)") String url,
            @ToolParam(description = "Optional HTTP headers in format: header1=value1&header2=value2") String headers,
            @ToolParam(description = "Optional query parameters in format: param1=value1&param2=value2") String queryParams) {

        println("→ HTTP DELETE: $url")

        String fullUrl = buildUrl(url, queryParams)
        def requestBuilder = HttpRequest.newBuilder(URI.create(fullUrl))
                .DELETE()
                .timeout(Duration.ofSeconds(30))

        applyHeaders(requestBuilder, headers)

        return executeRequest(requestBuilder.build())
    }

    private String buildUrl(String baseUrl, String queryParams) {
        if (!queryParams || queryParams.trim().isEmpty()) {
            return baseUrl
        }
        Map<String, String> params = parseKeyValuePairs(queryParams)
        String queryString = params.collect { k, v ->
            "${URLEncoder.encode(k, 'UTF-8')}=${URLEncoder.encode(v, 'UTF-8')}"
        }.join('&')

        return baseUrl.contains('?') ? "${baseUrl}&${queryString}" : "${baseUrl}?${queryString}"
    }

    private void applyHeaders(HttpRequest.Builder builder, String headers) {
        if (!headers || headers.trim().isEmpty()) {
            return
        }
        Map<String, String> parsed = parseKeyValuePairs(headers)
        parsed.each { key, value ->
            builder.header(key, value)
        }
    }

    private Map<String, String> parseKeyValuePairs(String input) {
        Map<String, String> result = new HashMap<>()
        if (!input || input.trim().isEmpty()) {
            return result
        }
        input.split('&').each { pair ->
            def idx = pair.indexOf('=')
            if (idx > 0) {
                def key = pair.substring(0, idx).trim()
                def value = pair.substring(idx + 1).trim()
                result[key] = value
            }
        }
        return result
    }

    private String executeRequest(HttpRequest request) {
        try {
            def response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            return buildSuccessResponse(response)
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt()
            return buildErrorResponse("Request interrupted: ${e.message}")
        } catch (HttpTimeoutException e) {
            return buildErrorResponse("Request timeout: ${e.message}")
        } catch (IOException e) {
            return buildErrorResponse("Connection error: ${e.message}")
        } catch (Exception e) {
            return buildErrorResponse("Error: ${e.message}")
        }
    }

    private String buildSuccessResponse(HttpResponse<String> response) {
        def headersJson = new StringBuilder('{')
        response.headers().map().each { key, values ->
            if (headersJson.length() > 1) headersJson.append(',')
            def value = values.join(',')
            headersJson.append("\"${key}\":\"${escapeJson(value)}\"")
        }
        headersJson.append('}')

        println("""{"success":true,"status":${response.statusCode()},"statusText":"${getStatusText(response.statusCode())}","body":"${escapeJson(response.body())}","headers":${headersJson.toString()}}""")

        return """{"success":true,"status":${response.statusCode()},"statusText":"${getStatusText(response.statusCode())}","body":"${escapeJson(response.body())}","headers":${headersJson.toString()}}"""
    }

    private String buildErrorResponse(String errorMessage) {
        return """{"success":false,"error":"${escapeJson(errorMessage)}","status":0}"""
    }

    private String getStatusText(int statusCode) {
        switch (statusCode) {
            case 200: return "OK"
            case 201: return "Created"
            case 204: return "No Content"
            case 301: return "Moved Permanently"
            case 302: return "Found"
            case 304: return "Not Modified"
            case 400: return "Bad Request"
            case 401: return "Unauthorized"
            case 403: return "Forbidden"
            case 404: return "Not Found"
            case 405: return "Method Not Allowed"
            case 409: return "Conflict"
            case 422: return "Unprocessable Entity"
            case 429: return "Too Many Requests"
            case 500: return "Internal Server Error"
            case 502: return "Bad Gateway"
            case 503: return "Service Unavailable"
            case 504: return "Gateway Timeout"
            default: return "Unknown"
        }
    }

    private String escapeJson(String text) {
        if (!text) return ""
        return text
                .replace('\\', '\\\\')
                .replace('"', '\\"')
                .replace('\n', '\\n')
                .replace('\r', '\\r')
                .replace('\t', '\\t')
    }
}
