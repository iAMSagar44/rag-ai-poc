module "ai_search" {
  source = "./modules/ai_search"

  RG  = var.RG
  sku = var.sku
}

module "app_service" {
  source = "./modules/app_service"

  RG                              = var.RG
  APP                             = var.APP
  APP_SERVICE_PLAN                = var.APP_SERVICE_PLAN
  SPRING_AI_AZURE_OPENAI_API_KEY  = var.SPRING_AI_AZURE_OPENAI_API_KEY
  SPRING_AI_AZURE_OPENAI_ENDPOINT = var.SPRING_AI_AZURE_OPENAI_ENDPOINT
  AZURE_AI_SEARCH_API_KEY         = var.AZURE_AI_SEARCH_API_KEY
  AZURE_AI_SEARCH_ENDPOINT        = var.AZURE_AI_SEARCH_ENDPOINT
  AZURE_AI_SEARCH_INDEX_NAME      = var.AZURE_AI_SEARCH_INDEX_NAME
}
