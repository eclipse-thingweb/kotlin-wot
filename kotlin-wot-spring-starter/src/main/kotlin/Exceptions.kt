package ai.anfc.lmos.wot.spring

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ThingNotFound(message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class PropertyNotFound(message: String?) : RuntimeException(message)

@ResponseStatus(HttpStatus.NOT_FOUND)
class ActionNotFound(message: String?) : RuntimeException(message)