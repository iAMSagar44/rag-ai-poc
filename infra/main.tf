module "ai_search" {
  source = "./modules/ai_search"

  resource_group_name = var.resource_group_name
  sku                 = var.sku
}

module "app_service" {
  source = "./modules/app_service"

  resource_group_name             = var.resource_group_name
  SPRING_AI_AZURE_OPENAI_API_KEY  = var.SPRING_AI_AZURE_OPENAI_API_KEY
  SPRING_AI_AZURE_OPENAI_ENDPOINT = var.SPRING_AI_AZURE_OPENAI_ENDPOINT
  AZURE_AI_SEARCH_API_KEY         = var.AZURE_AI_SEARCH_API_KEY
  AZURE_AI_SEARCH_ENDPOINT        = var.AZURE_AI_SEARCH_ENDPOINT
}
