package com.zhufucdev

import com.zhufucdev.data.Database
import com.zhufucdev.data.Database.isOp
import com.zhufucdev.data.SignUpRecord
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.command.getGroupOrNull
import net.mamoe.mirai.contact.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object GlobalCommand : CompositeCommand(
    Plugin, "class",
    description = "ClassBot指令集"
) {
    @SubCommand()
    suspend fun CommandSender.op(target: Member) {
        if (!isOp(target.group)) {
            return
        }
        Database.markAsAdmin(target)
        Database.sync()
        sendMessage("将${target.nick}设置为机器人管理员")
    }

    @SubCommand
    suspend fun CommandSender.classmate(str: String) {
        val targets = arrayListOf<Member>()
        val removal = arrayListOf<Member>()
        var split = str.split(',').map { it.trim() }
        val basement: Group = getGroupOrNull() ?: split.first().split('.')
            .let {
                suspend fun reportError() = sendMessage("参数错误: 在非群聊上下文中须指定目标群号，以\".\"为分隔")

                if (it.size != 2) {
                    reportError()
                    return
                }
                val id = it.first().toLongOrNull()
                if (id == null) {
                    reportError()
                    return
                }
                for (bot in Bot.instances) {
                    val index = bot.getGroup(id)
                    if (index != null) {
                        split = split.drop(1).plus(it[1])
                        return@let index
                    }
                }
                sendMessage("在所有在线机器人的联系人中找不到群${it.first()}")
                return
            }

        if (!isOp(basement)) {
            return
        }

        split.forEach { t ->
            if (t == "*") {
                targets.addAll(basement.members)
            } else {
                fun getMember(id: Long?) = if (id == null) {
                    val name = t.removePrefix("!").removePrefix("@")
                    basement.members.firstOrNull { (it.nameCard.isNotEmpty() && it.nameCard == name) || it.nick == name }
                } else {
                    basement.getMember(id)
                }

                if (!t.startsWith('!')) {
                    val id = t.toLongOrNull()
                    targets.add(getMember(id) ?: return@forEach)
                } else {
                    // exclude mode
                    val id = t.removePrefix("!").toLongOrNull()
                    val member = getMember(id) ?: return@forEach
                    val removed = targets.remove(member)
                    if (!removed) {
                        removal.add(member)
                    }
                }
            }
        }

        if (targets.isNotEmpty()) {
            Database.markAsClassmates(targets)
            sendMessage("向群中新增了${targets.size}个学员")
        }
        if (removal.isNotEmpty()) {
            Database.removeClassmates(removal)
            sendMessage("从群中移除了${removal.size}个学员")
        }
        Database.sync()
    }

    @SubCommand
    suspend fun CommandSender.signup(group: Group) {
        if (!isOp(group)) {
            return
        }
        val records = Database[group]
        val classmates = Database.getClassmates(group)
        sendMessage(
            "今日未签到者: ${
                buildString {
                    classmates.subtract(records.filter {
                        it is SignUpRecord && LocalDateTime.ofInstant(
                            it.timestamp,
                            ZoneId.systemDefault()
                        ).toLocalDate().isEqual(LocalDateTime.now().toLocalDate())
                    }.map { it.classmate }.toSet())
                        .forEach {
                            append(group.getMember(it)?.nameCardOrNick ?: it)
                            append(", ")
                        }
                    if (isNotEmpty()) {
                        delete(length - 2, length)
                    } else {
                        append("无")
                    }
                }
            }"
        )
    }

    @SubCommand
    suspend fun MemberCommandSender.signup() {
        signup(group)
    }
}