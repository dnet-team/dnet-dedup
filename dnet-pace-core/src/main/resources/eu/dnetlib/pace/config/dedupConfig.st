{
	"wf" : {
		"threshold" : "$threshold$",
		"dedupRun" : "$run$",
		"entityType" : "$entityType$",
		"subEntityType" : "$subEntityType$",
		"subEntityValue" : "$subEntityValue$",
		"orderField" : "$orderField$",
		"queueMaxSize" : "$queueMaxSize$",
		"groupMaxSize" : "$groupMaxSize$",
		"slidingWindowSize" : "$slidingWindowSize$",
		"rootBuilder" : [ $rootBuilder:{"$it$"};separator=", "$ ],
        "includeChildren" : "$includeChildren$",
        "configurationId" : "$configurationId$"
	},
	"pace" : {
		"clustering" : [
		],
		"sufficientConditions" : [
		],
		"necessaryConditions" : [
		],
		"model" : [
		],
		"blacklists" : { }
	}
}