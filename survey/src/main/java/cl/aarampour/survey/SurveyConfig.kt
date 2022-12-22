package cl.aarampour.survey

class SurveyConfig {
    companion object{
        /**
         * Camera configuration
         */
        public var IS_PDF_ALLOWED = false
        public var IS_GALLERY_ALLOWED = true

        public fun isPDFAllowed() : Boolean{
            return IS_PDF_ALLOWED
        }

        public fun isGalleryAllowed() : Boolean{
            return IS_GALLERY_ALLOWED
        }
    }
}