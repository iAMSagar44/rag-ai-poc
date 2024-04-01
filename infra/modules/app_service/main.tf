data "azurerm_client_config" "current" {

}

resource "azurerm_service_plan" "appserviceplan" {
  name                = "rag-appplan-rs"
  location            = var.resource_group_location
  resource_group_name = var.resource_group_name
  os_type             = "Linux"
  sku_name            = "B1"
  tags = {
    project = "rag"
    type    = "poc"
  }
}

#Create the back-end app, pass in the App Service Plan ID
resource "azurerm_linux_web_app" "backendapp" {
  name                = "rag-backendapp-rs"
  resource_group_name = var.resource_group_name
  location            = var.resource_group_location
  service_plan_id     = azurerm_service_plan.appserviceplan.id
  https_only          = true
  site_config {
    application_stack {
      java_server         = "JAVA"
      java_version        = "17"
      java_server_version = "17"
    }
  }
  identity {
    type = "SystemAssigned"
  }

  app_settings = {
    "SPRING_AI_AZURE_OPENAI_API_KEY"  = "@Microsoft.KeyVault(VaultName=${azurerm_key_vault.kv_account_01.name};SecretName=${azurerm_key_vault_secret.kv_secret_01.name})"
    "SPRING_AI_AZURE_OPENAI_ENDPOINT" = var.SPRING_AI_AZURE_OPENAI_ENDPOINT
    "AZURE_AI_SEARCH_API_KEY"         = "@Microsoft.KeyVault(VaultName=${azurerm_key_vault.kv_account_01.name};SecretName=${azurerm_key_vault_secret.kv_secret_02.name})"
    "AZURE_AI_SEARCH_ENDPOINT"        = var.AZURE_AI_SEARCH_ENDPOINT
  }
  tags = {
    project = "rag"
    type    = "poc"
  }
}
