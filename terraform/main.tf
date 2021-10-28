data "local_file" "buildJar" {
  filename    = "../build/libs/http-cloudfunction-java-gradle-all.jar"
}

# Create bucket that will host the source code
resource "google_storage_bucket" "bucket" {
  name = "${var.project}-function"
}

# Add source code zip to bucket
resource "google_storage_bucket_object" "functionGcs" {
  bucket = google_storage_bucket.bucket.name
  source = data.local_file.buildJar
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
resource "google_cloudfunctions_function" "function" {
  name    = var.name
  runtime = "java11"

  available_memory_mb   = 256

  source_archive_bucket = google_storage_bucket.bucket.name
  source_archive_object = google_storage_bucket_object.functionGcs.name
  trigger_http          = true
  entry_point           = var.entry_point
}

# Create IAM entry so all users can invoke the function
resource "google_cloudfunctions_function_iam_member" "invoker" {
  project        = google_cloudfunctions_function.function.project
  region         = google_cloudfunctions_function.function.region
  cloud_function = google_cloudfunctions_function.function.name

  role   = "roles/cloudfunctions.invoker"
  member = "allUsers"
}