variable "resource_group_location" {
  type        = string
  description = "Location for all resources."
  default     = "australiaeast"
}

variable "resource_group_name" {
  type        = string
  description = "Name of the existing resource group"
}


variable "sku" {
  description = "The pricing tier of the search service you want to create (for example, basic or standard)."
  type        = string
  validation {
    condition     = contains(["free", "basic"], var.sku)
    error_message = "The sku must be one of the following values for PoC purposes: free or basic"
  }
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
