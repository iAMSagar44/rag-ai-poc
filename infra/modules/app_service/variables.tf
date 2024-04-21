variable "resource_group_location" {
  type        = string
  description = "Location for all resources."
  default     = "australiaeast"
}

variable "RG" {
  type        = string
  description = "Name of the existing resource group"
}

variable "APP" {
  type        = string
  description = "Name of the web app service name"
}

variable "APP_SERVICE_PLAN" {
  type        = string
  description = "Name of the app service plan"
}

variable "SPRING_AI_AZURE_OPENAI_API_KEY" {
  type = string
}

variable "SPRING_AI_AZURE_OPENAI_ENDPOINT" {
  type = string
}

variable "AZURE_AI_SEARCH_API_KEY" {
  type = string
}

variable "AZURE_AI_SEARCH_ENDPOINT" {
  type = string
}

variable "AZURE_AI_SEARCH_INDEX_NAME" {
  type = string
}
