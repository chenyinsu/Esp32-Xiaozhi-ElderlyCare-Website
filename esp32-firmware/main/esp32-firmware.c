#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_wifi.h"
#include "nvs_flash.h"
#include "esp_http_client.h"
#include "cJSON.h"
#include "esp_timer.h"

static const char *TAG = "ESP32-ROBOT";

// ==================== 配置区 ====================
// WiFi 配置（电脑热点）
#define WIFI_SSID      "米粒儿的iPhone"       // 改成你的电脑热点名称
#define WIFI_PASSWORD  "20050121"             // 改成你的电脑热点密码

// 后端服务器地址（热点模式下电脑 IP 固定为 192.168.137.1）
#define SERVER_IP      "192.168.137.1"
#define SERVER_PORT    8080

// 设备信息
#define DEVICE_ID      "esp32-s3-001"
#define DEVICE_NAME    "暖心伴机器人"

// ==================== 全局变量 ====================
static bool wifi_connected = false;
static uint32_t device_db_id = 0;  // 后端返回的设备数据库ID

// ==================== WiFi 事件处理 ====================
static void wifi_event_handler(void *arg, esp_event_base_t event_base,
                                int32_t event_id, void *event_data) {
    if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_START) {
        esp_wifi_connect();
    } else if (event_base == WIFI_EVENT && event_id == WIFI_EVENT_STA_DISCONNECTED) {
        wifi_connected = false;
        ESP_LOGW(TAG, "WiFi disconnected, reconnecting...");
        esp_wifi_connect();
    } else if (event_base == IP_EVENT && event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t *event = (ip_event_got_ip_t *)event_data;
        wifi_connected = true;
        ESP_LOGI(TAG, "✅ WiFi Connected! IP: " IPSTR, IP2STR(&event->ip_info.ip));
    }
}

// ==================== 初始化 WiFi ====================
static void init_wifi(void) {
    nvs_flash_init();
    esp_netif_init();
    esp_event_loop_create_default();
    esp_netif_create_default_wifi_sta();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    esp_wifi_init(&cfg);

    esp_event_handler_instance_register(WIFI_EVENT, ESP_EVENT_ANY_ID,
                                        &wifi_event_handler, NULL, NULL);
    esp_event_handler_instance_register(IP_EVENT, IP_EVENT_STA_GOT_IP,
                                        &wifi_event_handler, NULL, NULL);

    wifi_config_t wifi_config = {
        .sta = {
            .ssid = WIFI_SSID,
            .password = WIFI_PASSWORD,
        },
    };
    esp_wifi_set_mode(WIFI_MODE_STA);
    esp_wifi_set_config(WIFI_IF_STA, &wifi_config);
    esp_wifi_start();
}

// ==================== HTTP 响应解析 ====================
static bool parse_device_id_from_response(const char *response, uint32_t *out_id) {
    cJSON *root = cJSON_Parse(response);
    if (!root) return false;

    cJSON *data = cJSON_GetObjectItem(root, "data");
    if (data) {
        cJSON *id = cJSON_GetObjectItem(data, "id");
        if (id && cJSON_IsNumber(id)) {
            *out_id = id->valueint;
            cJSON_Delete(root);
            return true;
        }
    }

    cJSON_Delete(root);
    return false;
}

// ==================== HTTP 请求封装 ====================
static char* http_request(const char *method, const char *path, const char *json_body) {
    if (!wifi_connected) {
        ESP_LOGW(TAG, "WiFi not connected, skip request");
        return NULL;
    }

    char url[512];
    snprintf(url, sizeof(url), "http://%s:%d%s", SERVER_IP, SERVER_PORT, path);

    ESP_LOGI(TAG, "📡 %s %s", method, url);

    esp_http_client_config_t config = {
        .url = url,
        .timeout_ms = 5000,
        .buffer_size = 1024,
    };

    esp_http_client_handle_t client = esp_http_client_init(&config);

    // 设置请求方法
    if (strcmp(method, "POST") == 0) {
        esp_http_client_set_method(client, HTTP_METHOD_POST);
    } else if (strcmp(method, "PATCH") == 0) {
        esp_http_client_set_method(client, HTTP_METHOD_PATCH);
    } else {
        esp_http_client_set_method(client, HTTP_METHOD_GET);
    }

    // 设置请求头
    esp_http_client_set_header(client, "Content-Type", "application/json");

    // 设置请求体
    if (json_body) {
        esp_http_client_set_post_field(client, json_body, strlen(json_body));
    }

    esp_err_t err = esp_http_client_perform(client);

    char *response = NULL;
    if (err == ESP_OK) {
        int status_code = esp_http_client_get_status_code(client);
        int content_length = esp_http_client_get_content_length(client);

        ESP_LOGI(TAG, "✅ %s %s -> Status: %d", method, path, status_code);

        if (content_length > 0) {
            response = malloc(content_length + 1);
            if (response) {
                int read_len = esp_http_client_read(client, response, content_length);
                response[read_len] = '\0';
                ESP_LOGI(TAG, "📨 Response: %s", response);
            }
        }
    } else {
        ESP_LOGE(TAG, "❌ %s %s failed: %s", method, path, esp_err_to_name(err));
    }

    esp_http_client_cleanup(client);
    return response;
}

// ==================== 业务功能 ====================

// 1. 设备注册（POST /api/devices）
static bool register_device(void) {
    char json[512];
    snprintf(json, sizeof(json),
        "{"
        "\"deviceId\":\"%s\","
        "\"deviceName\":\"%s\","
        "\"status\":\"ONLINE\","
        "\"batteryLevel\":%d,"
        "\"wifiStrength\":%d,"
        "\"volumeLevel\":50,"
        "\"hasCamera\":false"
        "}",
        DEVICE_ID, DEVICE_NAME, 95, 80);

    char *response = http_request("POST", "/api/devices", json);
    if (!response) return false;

    bool success = parse_device_id_from_response(response, &device_db_id);
    if (success) {
        ESP_LOGI(TAG, "✅ Device registered with ID: %lu", device_db_id);
    }

    free(response);
    return success;
}

// 2. 更新设备状态（PATCH /api/devices/{id}/status）
static void update_device_status(const char *status, int battery) {
    if (device_db_id == 0) {
        if (!register_device()) return;
    }

    char path[128];
    snprintf(path, sizeof(path), "/api/devices/%lu/status", device_db_id);

    char json[256];
    snprintf(json, sizeof(json),
        "{"
        "\"status\":\"%s\","
        "\"batteryLevel\":%d"
        "}",
        status, battery);

    char *response = http_request("PATCH", path, json);
    if (response) {
        ESP_LOGI(TAG, "✅ Device status updated: %s", status);
        free(response);
    }
}

// 3. 发送紧急事件（POST /api/emergencies）
static void send_emergency(const char *type, const char *level, const char *description) {
    char json[512];
    snprintf(json, sizeof(json),
        "{"
        "\"deviceId\":\"%s\","
        "\"emergencyType\":\"%s\","
        "\"emergencyLevel\":\"%s\","
        "\"description\":\"%s\","
        "\"triggerSource\":\"button\""
        "}",
        DEVICE_ID, type, level, description);

    char *response = http_request("POST", "/api/emergencies", json);
    if (response) {
        ESP_LOGW(TAG, "🚨 Emergency sent: %s - %s", type, description);
        free(response);
    }
}

// ==================== 模拟按钮任务 ====================
static void button_simulator_task(void *pvParameters) {
    int counter = 0;

    // 等待WiFi连接和注册完成
    vTaskDelay(8000 / portTICK_PERIOD_MS);

    while (1) {
        vTaskDelay(20000 / portTICK_PERIOD_MS);  // 每20秒

        counter++;

        if (counter % 3 == 0) {
            ESP_LOGW(TAG, "🔴 模拟紧急按钮按下！");
            send_emergency("BUTTON_PRESS", "HIGH", "用户按下紧急求助按钮");
        } else if (counter % 3 == 1) {
            ESP_LOGW(TAG, "⚠️ 模拟跌倒检测！");
            send_emergency("FALL_DETECTION", "CRITICAL", "检测到可能的跌倒事件");
        } else {
            ESP_LOGW(TAG, "💊 模拟健康异常！");
            send_emergency("HEALTH_ABNORMAL", "MEDIUM", "心率异常，请关注");
        }
    }
}

// ==================== 心跳任务 ====================
static void heartbeat_task(void *pvParameters) {
    vTaskDelay(5000 / portTICK_PERIOD_MS);

    register_device();

    int battery = 100;

    while (1) {
        if (wifi_connected && device_db_id > 0) {
            update_device_status("ONLINE", battery);
        } else if (wifi_connected && device_db_id == 0) {
            register_device();
        }

        battery = (battery > 5) ? battery - 1 : 100;

        vTaskDelay(30000 / portTICK_PERIOD_MS);  // 每30秒心跳
    }
}

// ==================== 主函数 ====================
void app_main(void) {
    ESP_LOGI(TAG, "🚀 ESP32-S3 Robot [%s] Starting...", DEVICE_ID);
    ESP_LOGI(TAG, "📡 Server: %s:%d", SERVER_IP, SERVER_PORT);

    init_wifi();

    xTaskCreate(heartbeat_task, "heartbeat", 4096, NULL, 5, NULL);
    xTaskCreate(button_simulator_task, "button_sim", 4096, NULL, 4, NULL);

    int counter = 0;
    while (1) {
        ESP_LOGI(TAG, "💓 Running... [%d] | WiFi: %s | Device ID: %lu",
                 counter++,
                 wifi_connected ? "✅" : "❌",
                 device_db_id);
        vTaskDelay(10000 / portTICK_PERIOD_MS);
    }
}