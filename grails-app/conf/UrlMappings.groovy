class UrlMappings {

	static mappings = {
        "/"(controller:"top", action:"index")
        "/backdoor"(controller:"admin", action:"index")
		"/$controller/$action?/$id?"{
			constraints {
				// apply constraints here
			}
		}

		"500"(view:'/error')
	}
}
