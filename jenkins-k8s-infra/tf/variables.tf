variable "project" {
    type = string
    default = "gcp101730-pulanowskisandbox"
    }

variable "region" {
    type = string
    default = "us-central1"
    }

variable "zone" {
    type = string
    default = "us-central1-a"
    }

variable "credentials_file_path" {
  description = "The path to the service account key file."
  type        = string
  default     = "../keys/sa-terraform.json"
}

#for k8s secrets and namespace

variable "gcp-sa"{
    description = "gcp-sa key path"
    type        = string
    default     = "../keys/sa-jenkins.json"
}

variable "gcp-kh"{
    description = "gcp-kh key path"
    type        = string
    default     = "../keys/known_hosts"
}

variable "gcp-ssh"{
    description = "gcp-ssh key path"
    type        = string
    default     = "../keys/gcp_ssh"
}

variable "namespace"{
    description = "k8s namespace"
    type        = string
    default     = "devops-tools"
}