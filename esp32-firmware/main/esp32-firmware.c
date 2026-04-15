#include <stdio.h>
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"
#include "esp_wifi.h"
#include "nvs_flash.h"
#include "esp_http_client.h"

static const char *TAG = "ESP32-Robot";
static const char *WIFI_SSID = "YOUR_WIFI_SSID";
static const char *WIFI_PASS = "YOUR_WIFI_PASSWORD";
static const char *API_URL = "http://192.168.x.x:8080/api/ping";

static void wifi_event_handler(void *arg, esp_event_base_t event_base,
                                int32_t event_id, void *event_data) {
    if (event_id == WIFI_EVENT_STA_START) {
        esp_wifi_connect();
    } else if (event_id == IP_EVENT_STA_GOT_IP) {
        ip_event_got_ip_t *event = (ip_event_got_ip_t *) event_data;
        ESP_LOGI(TAG, "✅ WiFi Connected! IP: " IPSTR, IP2STR(&event->ip_info.ip));
    }
}

static void init_wifi(void) {
    nvs_flash_init();
    esp_netif_init();
    esp_event_loop_create_default();
    esp_netif_create_default_wifi_sta();

    wifi_init_config_t cfg = WIFI_INIT_CONFIG_DEFAULT();
    esp_wifi_init(&cfg);

    esp_event_handler_instance_register(WIFI_EVENT, ESP_EVENT_ANY_ID,
                                       &wifi_event_handler, NULL, NULL);

    wifi_config_t wifi_config = {
        .sta = {
            .ssid = WIFI_SSID,
            .password = WIFI_PASS,
        },
    };
    esp_wifi_set_mode(WIFI_MODE_STA);
    esp_wifi_set_config(WIFI_IF_STA, &wifi_config);
    esp_wifi_start();
}

static esp_err_t http_event_handler(esp_http_client_event_t *evt) {
    switch(evt->event_id) {
        case HTTP_EVENT_ON_DATA:
            ESP_LOGI(TAG, "📨 Response: %.*s", evt->data_len, (char *)evt->data);
            break;
        case HTTP_EVENT_ON_FINISH:
            ESP_LOGI(TAG, "✅ HTTP Request Complete");
            break;
        default:
            break;
    }
    return ESP_OK;
}

static void http_test_task(void *pvParameters) {
    vTaskDelay(5000 / portTICK_PERIOD_MS);  // 等待WiFi连接

    esp_http_client_config_t config = {
        .url = API_URL,
        .event_handler = http_event_handler,
    };
    esp_http_client_handle_t client = esp_http_client_init(&config);

    esp_http_client_set_method(client, HTTP_METHOD_GET);
    esp_http_client_perform(client);
    esp_http_client_cleanup(client);

    vTaskDelete(NULL);
}

void app_main(void) {
    ESP_LOGI(TAG, "🚀 ESP32-S3 Robot Starting...");
    
    init_wifi();
    
    xTaskCreate(http_test_task, "http_test", 4096, NULL, 5, NULL);

    int counter = 0;
    while(1) {
        ESP_LOGI(TAG, "[Running] Counter: %d", counter++);
        vTaskDelay(3000 / portTICK_PERIOD_MS);
    }
}