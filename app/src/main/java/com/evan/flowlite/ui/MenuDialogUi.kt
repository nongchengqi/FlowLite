package com.evan.flowlite.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.window.SecureFlagPolicy

@Composable
fun DropdownMenuView(state: MutableState<Boolean>,offset: DpOffset,menu:List<Pair<ImageVector,String>>,itemClick:(String)->Unit = {}){
        DropdownMenu(
            expanded = state.value,
            onDismissRequest = {
                state.value = false
            },
            offset = offset,
            properties = PopupProperties()
        ) {
            menu.forEach {
                DropdownMenuItem(state, it.first,it.second){
                    itemClick.invoke(it.second)
                }
            }
        }
}
@Composable
fun FlowItemMenuView(state: MutableState<Boolean>,offset: DpOffset,isFree:Boolean,itemClick:(String)->Unit = {}){
    DropdownMenu(
        expanded = state.value,
        onDismissRequest = {
            state.value = false
        },
        offset = offset,
        properties = PopupProperties()
    ) {
        val title = if (isFree) "移动到普通流量" else "移动到免费流量"
        DropdownMenuItem(state, Icons.Filled.Autorenew,title){
            itemClick.invoke(title)
        }
    }
}

@Composable
fun DropdownMenuItem(state: MutableState<Boolean>, icon: ImageVector, text:String, click:()->Unit){
    DropdownMenuItem(
        onClick = {
            click.invoke()
            state.value = false
        },
        enabled = true,
    ) {
        Icon(imageVector = icon, contentDescription = text)
        Text(text = text,modifier = Modifier.padding(start = 10.dp))
    }
}

@Composable
fun LogRecordDialog(log:String,onDismiss:()->Unit ={}) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            securePolicy = SecureFlagPolicy.SecureOff
        )
    ) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).background(color = Color.White, shape = RoundedCornerShape(10.dp)).padding(horizontal = 30.dp, vertical = 12.dp)) {
            SelectionContainer {
                Text(
                    text = log.ifEmpty { "暂无日志，请打开日志记录开关" },
                    fontSize = 16.sp,
                    color = Color(0xff333333),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}