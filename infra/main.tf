resource "azurerm_search_service" "search" {
  name                = var.azurerm_search_service_name
  resource_group_name = var.resource_group_name
  location            = var.resource_group_location
  sku                 = var.sku
  replica_count       = var.replica_count
  partition_count     = var.partition_count
  tags = {
    project = "rag"
    type    = "poc"
  }
}
