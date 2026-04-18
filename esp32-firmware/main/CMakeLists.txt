idf_component_register(SRCS "esp32-firmware.c"
                    INCLUDE_DIRS "."
                    REQUIRES esp_wifi esp_http_client nvs_flash esp_timer)