variable "resource_group_location" {
  type        = string
  description = "Location for all resources."
  default     = "australiaeast"
}

variable "RG" {
  type        = string
  description = "Name of the existing resource group"
}

variable "azurerm_search_service_name" {
  type        = string
  description = "Name of the Azure Search service"
  default     = "rag-docsearch-rs"
}


variable "sku" {
  description = "The pricing tier of the search service you want to create (for example, basic or standard)."
  type        = string
  validation {
    condition     = contains(["free", "basic"], var.sku)
    error_message = "The sku must be one of the following values for PoC purposes: free or basic"
  }
  default = "basic"
}

variable "replica_count" {
  type        = number
  description = "Replicas distribute search workloads across the service. You need at least two replicas to support high availability of query workloads (not applicable to the free tier)."
  default     = 1
  validation {
    condition     = var.replica_count >= 1 && var.replica_count <= 3
    error_message = "The replica_count must be between 1 and 3 for PoC purposes."
  }
}

variable "partition_count" {
  type        = number
  description = "Partitions allow for scaling of document count as well as faster indexing by sharding your index over multiple search units."
  default     = 1
  validation {
    condition     = contains([1, 2, 3], var.partition_count)
    error_message = "The partition_count must be one of the following values for PoC purposes: 1, 2, 3"
  }
}