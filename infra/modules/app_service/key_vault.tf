// ===========azurerm_key_vault_01===========
resource "azurerm_key_vault" "kv_account_01" {
  location            = var.resource_group_location
  name                = "rag-key-vault-01-rs"
  resource_group_name = var.resource_group_name
  sku_name            = "standard"
  tenant_id           = data.azurerm_client_config.current.tenant_id
  tags = {
    project = "rag"
    type    = "poc"
  }
}


resource "azurerm_key_vault_access_policy" "user" {
  key_vault_id = azurerm_key_vault.kv_account_01.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = data.azurerm_client_config.current.object_id

  secret_permissions = [
    "Get",
    "List",
    "Set",
    "Purge",
    "Delete"
  ]
}

resource "azurerm_key_vault_access_policy" "app" {
  key_vault_id = azurerm_key_vault.kv_account_01.id
  tenant_id    = data.azurerm_client_config.current.tenant_id
  object_id    = azurerm_linux_web_app.backendapp.identity.0.principal_id

  secret_permissions = [
    "Get",
  ]
}

resource "azurerm_key_vault_secret" "kv_secret_01" {
  key_vault_id = azurerm_key_vault.kv_account_01.id
  name         = "az-openai-key"
  value        = var.SPRING_AI_AZURE_OPENAI_API_KEY
  depends_on   = [azurerm_key_vault_access_policy.user]
}

resource "azurerm_key_vault_secret" "kv_secret_02" {
  key_vault_id = azurerm_key_vault.kv_account_01.id
  name         = "az-searchai-key"
  value        = var.AZURE_AI_SEARCH_API_KEY
  depends_on   = [azurerm_key_vault_access_policy.user]
}
