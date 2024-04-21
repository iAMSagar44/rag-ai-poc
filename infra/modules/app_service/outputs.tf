output "backendapp_name" {
  value = azurerm_linux_web_app.backendapp.name
}

output "backendapp_hostname" {
  value = azurerm_linux_web_app.backendapp.default_hostname
}

output "backendapp_identity" {
  value     = azurerm_linux_web_app.backendapp.identity.0.principal_id
  sensitive = true
}