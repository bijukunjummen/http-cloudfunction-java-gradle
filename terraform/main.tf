provider "google" {
  project = var.project
  region  = var.region
}
data "archive_file" "source" {
  type        = "zip"
  source_dir  = "../build/libs"
  output_path = "/tmp/http-cloudfunction-java-gradle-all.zip"
}


# Create bucket that will host the source code
resource "google_storage_bucket" "bucket" {
  name     = "${var.project}-function"
  force_destroy = true
  location = "US"
  uniform_bucket_level_access = true
}

# Add source code zip to bucket
resource "google_storage_bucket_object" "functionGcs" {
  name   = "http-cloudfunction-java-gradle-all.zip"
  bucket = google_storage_bucket.bucket.name
  source = data.archive_file.source.output_path
}

# Enable Cloud Functions API
resource "google_project_service" "cf" {
  project = var.project
  service = "cloudfunctions.googleapis.com"

  disable_dependent_services = true
  disable_on_destroy         = false
}

# Enable Cloud Build API
resource "google_project_service" "cb" {
  project = var.project
  service = "cloudbuild.googleapis.com"

  disable_dependent_services = true
  disable_on_destroy         = false
}

# Create Cloud Function
resource "google_cloudfunctions2_function" "function" {
  provider = google-beta
  name    = var.name
  project = var.project
  location = var.region

  build_config {
    runtime = "java17"
    entry_point           = var.entry_point
    source {
      storage_source {
        bucket = google_storage_bucket.bucket.name
        object = google_storage_bucket_object.functionGcs.name
      }
    }
  }
  service_config {
    max_instance_count  = 1
    available_memory    = "256M"
    timeout_seconds     = 60
  }
}

# Create IAM entry so all users can invoke the function
resource "google_cloudfunctions_function_iam_member" "invoker" {
  project        = google_cloudfunctions2_function.function.project
  region         = google_cloudfunctions2_function.function.location
  cloud_function = google_cloudfunctions2_function.function.name

  role   = "roles/cloudfunctions.invoker"
  member = "allUsers"
}