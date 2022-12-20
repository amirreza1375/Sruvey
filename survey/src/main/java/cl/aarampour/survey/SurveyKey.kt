package cl.aarampour.survey

class SurveyKey {

    companion object{
        val PAGE_POSITION = "pagePositions"
    }

    class IMAGE_PROPS{
        companion object{
            val VIEW_ID = "id"
            val VIEW_NAME = "name"
            val PAGE_POSITION = "position"
            val IMAGE_TYPE_ID = "imageType"
            val IMAGE_TYPE_NAME = "imageTypeName"
            val INDEX = "index"
            val PATH = "path"

            val FOLDER_PATH = "folderPath"
        }
    }

    class TYPE {
        companion object {
            val RADIO_GROUP = "radiogroup"
            val CHECK_BOX = "checkbox"
            val IMAGE_TAKER = "file"
            val COMMENT = "comment"
            val HTML = "html"
            val SIMPLE_TEXT = "simpletext"
            val MULTI_TEXT = "multipletext"
            val MULTI_TEXT_SERVER = "multitext"
            val IMAGE_SLIDER = "imagepicker"
            val IMAGE_SLIDER_TYPE = "Database"
            val IMAGE_SLIDER_TYPE_OPTICO = "Optico"
        }
    }

    class Page {
        companion object {

            val VISIBLE_IF = "visibleIf"
            val TITLE = "title"
            val ELEMENTS = "elements"
            val PAGE_POSITION = "pagePosition"

        }

        class View{
            companion object{
                val TYPE = "type"
                val TITLE = "title"
                val IS_MANDATORY = "isRequired"
                val SYSTEM_ID = "SystemId"
                val ID = "id"
                val NAME = "name"
                val TIPO = "tipo"
                val IS_VISIBLE = "visible"
                val ENABLE_IF = "enableIf"
                val VISIBLE_IF = "visibleIf"
                val REQUIRED_IF = "requiredIf"
            }


            class RadioGroup{
                companion object{

                    val VALUE = "value"
                    val TEXT = "text"
                    val CHOICES = "choices"
                    val INDEX = "index"

                }
            }

            class CheckBox{
                companion object{
                    val VALUE = "value"
                    val CHOICES = "choices"
                    val STATUS = "status"
                    val TEXT = "text"
                    val INDEX = "index"
                    val DISABLE_OTHERS = "disableOther"
                }
            }

            class ImagesTaker{
                companion object{
                    val ID = "id"
                    val VALUE = "value"
                    val IMAGE_TYPE_NAME = "imagetypeName"
                    val COUNT = "Cuantos"
                    val IMAGE_TYPE = "imagetype"
                }

                class ImageTakerItem{
                    companion object{
                        val INDEX = "index"
                    }


                }

            }

            class SimpleText {
                companion object{
                    val HTML = "html"
                }
            }

            class Comment{
                companion object{
                    val VALUE = "value"
                    val MAX_LENGTH = "maxLength"
                }
            }

            class Slider{
                companion object {

                    val ID = "ID"
                    val POSICTION = "Posicion"
                    val ELEMENTO = "Elemento"
                    val SUBCANAL = "subcanal"
                }
            }

            class MultiText{
                companion object{
                    val PREVIEW = "Preview"
                    val VERTICAL = "Vertical"
                    val ITEMS = "items"
                    val VALUE = "value"
                    val ACCEPTABLE_VALUES = "AcceptableValues"

                }
                class Item{
                    companion object{
                        val NAME = "name"
                        val TITLE = "title"
                        val VALUE = "value"
                        val VALIDATORS = "validators"
                    }

                    class Validators{
                        companion object{
                            val TEXT = "text"
                            val MIN = "minValue"
                            val MAX = "maxValue"
                            val INPUT_TYPE = "type"
                            val NUMBER = "numeric"
                        }
                    }
                }
            }
        }
    }
}