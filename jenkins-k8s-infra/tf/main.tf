terraform {
  required_version = ">= 0.12"
  required_providers {
    time = {
      source  = "hashicorp/time"
    }
  }
}

provider "google" {
  project    = var.project
  region     = var.region
  zone       = var.zone
  credentials = file(var.credentials_file_path)
}


resource "google_compute_network" "gke-jenkins" {
  name                    = "gke-jenkins"
}

resource "google_compute_subnetwork" "gke-jenkins" {
  depends_on    = [google_compute_network.gke-jenkins]
  name          = "gke-jenkins-subnet"
  region        = var.region
  network       = "gke-jenkins"
  ip_cidr_range = "10.0.0.0/24"
}

resource "google_compute_firewall" "k8s-jenkins" {
  depends_on = [google_compute_subnetwork.gke-jenkins]
  name    = "k8s-jenkins"
  network = google_compute_network.gke-jenkins.self_link

  allow {
    protocol = "tcp"
    ports    = ["32000"]
  }

  source_ranges = ["0.0.0.0/0"]
}

resource "google_container_cluster" "jenkins-cluster" {
  name               = "jenkins-cluster"
  location           = "us-central1-a"
  initial_node_count = 1
  network            = "gke-jenkins"
  subnetwork         = google_compute_subnetwork.gke-jenkins.self_link
  node_config {
    machine_type = "e2-standard-8"
    tags         = ["gke-jenkins"]
  }
}

resource "null_resource" "jenkins-cluster" {
  depends_on = [google_container_cluster.jenkins-cluster]
  provisioner "local-exec" {
    command = "gcloud container clusters get-credentials jenkins-cluster --zone=us-central1-a"
  }
}

resource "null_resource" "kubectl_commands" {
  depends_on = [null_resource.jenkins-cluster]

  provisioner "local-exec" {
    command = "kubectl create namespace ${var.namespace}"
  }

  provisioner "local-exec" {
    command = "kubectl config set-context --current --namespace=${var.namespace}"
  }

  provisioner "local-exec" {
    command = "kubectl create secret generic gcp-sa --from-file=${var.gcp-sa}"
  }

  provisioner "local-exec" {
    command = "kubectl create secret generic gcp-ssh --from-file=${var.gcp-ssh}"
  }

  provisioner "local-exec" {
    command = "kubectl create secret generic gcp-kh --from-file=${var.gcp-kh}"
  }

  provisioner "local-exec" {
    command = "kubectl apply -f ../k8s/service-account.yaml"
  }

  provisioner "local-exec" {
    command = "kubectl apply -f ../k8s/deployment.yaml"
  }

}

