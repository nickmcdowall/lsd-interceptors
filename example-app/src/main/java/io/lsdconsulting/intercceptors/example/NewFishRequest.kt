package io.lsdconsulting.intercceptors.example

data class NewFishRequest(
    var name: String?
) {

    constructor() : this(null)

}
