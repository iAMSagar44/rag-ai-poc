variable "resource_group_location" {
  type        = string
  description = "Location for all resources."
  default     = "australiaeast"
}

variable "resource_group_name" {
  type        = string
  description = "Name of the existing resource group"
}

variable "SPRING_AI_AZURE_OPENAI_API_KEY" {
  type        = string
}

variable "SPRING_AI_AZURE_OPENAI_ENDPOINT" {
  type        = string
}

variable "AZURE_AI_SEARCH_API_KEY" {
  type        = string
}

variable "AZURE_AI_SEARCH_ENDPOINT" {
  type        = string
}