{ 
	"wf" : { 
		"configurationId" : "$configurationId$",
        "threshold" : "$threshold$", 
        "run" : "$run$", 
        "entityType" : "$entityType$", 
        "orderField" : "$orderField$", 
        "queueMaxSize" : "$queueMaxSize$",
        "groupMaxSize" : "$groupMaxSize$",
        "slidingWindowSize" : "$slidingWindowSize$",
        "rootBuilder" : [ $rootBuilder:{"$it$"};separator=", "$ ],
        "includeChildren" : "$includeChildren$" 
    },
	"pace" : {		
		"clustering" : [
		],		
  		"conditions" : [ 
  		],		
		"model" : [
		],
		"blacklists" : { } 		
	}
}