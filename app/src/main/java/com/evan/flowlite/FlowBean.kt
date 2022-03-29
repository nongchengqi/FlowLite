package com.evan.flowlite

data class InfoBean(var pkgName:String, var mlFlow:List<FlowBean>, var normalFlow:List<FlowBean>, var remindFlow:String, var useMlFlow:String, var useResFlow:String, var useTotalFlow:String)
data class FlowBean(val name:String,val total:String,val used:String,val remind:String)