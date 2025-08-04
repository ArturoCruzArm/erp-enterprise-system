# HashiCorp Vault Configuration for ERP System

# Storage Backend
storage "postgresql" {
  connection_url = "postgres://vault_user:vault_password@postgres:5432/vault_db?sslmode=disable"
  table         = "vault_kv_store"
  max_parallel  = 128
}

# High Availability
storage "raft" {
  path    = "/vault/data"
  node_id = "vault-node-1"
  
  retry_join {
    leader_api_addr = "https://vault-node-2:8200"
  }
  
  retry_join {
    leader_api_addr = "https://vault-node-3:8200"
  }
}

# Listener Configuration
listener "tcp" {
  address       = "0.0.0.0:8200"
  tls_cert_file = "/vault/certs/vault.crt"
  tls_key_file  = "/vault/certs/vault.key"
  tls_min_version = "tls12"
  
  # Security Headers
  tls_cipher_suites = "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_ECDSA_WITH_CHACHA20_POLY1305,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256"
}

# Cluster Configuration
cluster_addr = "https://vault-node-1:8201"
api_addr     = "https://vault-node-1:8200"

# UI Configuration
ui = true

# Telemetry
telemetry {
  prometheus_retention_time = "30s"
  disable_hostname = true
  
  statsd_address = "statsd:8125"
  
  circonus_api_token = ""
  circonus_api_app = "vault"
  circonus_api_url = "https://api.circonus.com/v2"
  circonus_submission_interval = "10s"
  circonus_submission_url = "https://trap.noit.circonus.net"
  circonus_check_id = ""
  circonus_check_force_metric_activation = "false"
  circonus_check_instance_id = ""
  circonus_check_search_tag = ""
  circonus_check_display_name = ""
  circonus_check_tags = ""
  circonus_broker_id = ""
  circonus_broker_select_tag = ""
}

# Entropy Augmentation
entropy "seal" {
  mode = "augmentation"
}

# Seal Configuration (Auto-unseal with cloud KMS)
seal "awskms" {
  region     = "us-west-2"
  kms_key_id = "alias/vault-unseal-key"
  endpoint   = "https://kms.us-west-2.amazonaws.com"
}

# Alternative: Azure Key Vault
# seal "azurekeyvault" {
#   tenant_id      = "46646709-b63e-4747-be42-516edeaf1e14"
#   client_id      = "03dc33fc-16d9-4b77-8152-3ec568f8af6e"
#   client_secret  = "DUJDS3..."
#   vault_name     = "hc-vault"
#   key_name       = "vault_key"
# }

# Logging
log_level = "INFO"
log_format = "json"
log_file = "/vault/logs/vault.log"
log_rotate_duration = "24h"
log_rotate_max_files = 15

# Performance and Limits
default_lease_ttl = "768h"
max_lease_ttl = "8760h"
default_max_request_duration = "90s"
disable_mlock = false
disable_cache = false

# Plugin Directory
plugin_directory = "/vault/plugins"

# License (Enterprise)
# license_path = "/vault/license/vault.hclic"

# Additional Security
disable_sealwrap = false
disable_indexing = false
disable_sentinel_trace = false

# Cache Size
cache_size = "131072"

# Raw Storage Options
raw_storage_endpoint = true
introspection_endpoint = true
unauthenticated_metrics_access = false

# Replication (Enterprise)
# replication {
#   performance {
#     mode = "primary"
#   }
#   dr {
#     mode = "primary"
#   }
# }