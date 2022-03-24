package com.zhufucdev

import com.zhufucdev.data.Database
import com.zhufucdev.data.Database.isOp
import com.zhufucdev.data.SignUpRecord
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.CompositeCommand
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.getMember
import net.mamoe.mirai.contact.nameCardOrNick
import java.time.Instant

typealias MemberDelegate = (Member) -> Boolean
typealias MemberIdDelegate = (Long) -> Boolean

object GlobalCommand : CompositeCommand(
    Plugin, "class",
    description = "ClassBot指令集"
) {
    @SubCommand
    suspend fun CommandSender.op(selector: String) {
        tryQuery(selector) {
            if (!isOp(basement)) {
                return@tryQuery
            }
            if (targets.isNotEmpty()) {
                sendMessage("将${buildTargetList { Database.markAsAdmin(it) }}设置为机器人管理员")
            }
            if (removal.isNotEmpty()) {
                sendMessage("取消了${buildRemovalList { Database.undoAdmin(it) }}的管理员权限")
            }
            Database.sync()
        }
    }

    @SubCommand
    suspend fun CommandSender.classmate(selector: String) {
        tryQuery(selector) {
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
    }

    @SubCommand
    suspend fun CommandSender.signup(group: Group) {
        if (!isOp(group)) {
            return
        }
        val classmates = Database.classmates(group)
        sendMessage(
            "今日未签到者: ${
                buildNames(
                    classmates,
                    include = { !SignUpRecord.hasSignedToday(it, group) },
                    nameGetter = { group.getMember(it)?.nameCardOrNick ?: it.toString() })

            }"
        )
    }

    @SubCommand
    suspend fun MemberCommandSender.signup() {
        signup(group)
    }

    @SubCommand
    suspend fun CommandSender.sign(selector: String) {
        tryQuery(selector) {
            if (targets.isNotEmpty()) {
                sendMessage(
                    "将${
                        buildTargetList {
                            Database.record(
                                basement,
                                SignUpRecord(it.id, Instant.now())
                            ); true
                        }
                    }标记为已签"
                )
            }
            if (removal.isNotEmpty()) {
                sendMessage("将${
                    buildRemovalList { 
                        Database.unrecord(basement) { r ->
                            (r is SignUpRecord) && r.classmate == it.id
                        }
                    }
                }标记为未签")
            }
            Database.sync()
        }
    }
}