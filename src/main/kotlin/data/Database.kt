package com.zhufucdev.data

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.zhufucdev.Plugin
import com.zhufucdev.serialization.gson
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.MemberCommandSender
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member

object Database {
    val configurationFile = Plugin.resolveDataFile("configuration.json")
    val currentFile = Plugin.resolveDataFile("records.json")
    val size get() = records.size

    private val configurations: MutableMap<Long, Configuration>
    private val records: MutableMap<Long, ArrayList<Record>> = hashMapOf()

    private var isRecordsChanged = false
    private var isConfigurationsChanged = false

    init {
        if (!currentFile.exists()) {
            currentFile.createNewFile()
            currentFile.writeText("{}")
        } else {
            val reader = currentFile.reader()
            val json = JsonParser.parseReader(reader).asJsonObject
            reader.close()
            json.entrySet().forEach { entry ->
                try {
                    val r = arrayListOf<Record>()
                    entry.value.asJsonArray.forEach {
                        r.add(
                            gson.fromJson(it, Record::class.java)
                        )
                    }

                    records[entry.key.toLong()] = r
                } catch (e: Exception) {
                    Plugin.logger.warning("Error while parsing record file: ", e)
                }
            }
        }

        if (!configurationFile.exists()) {
            configurationFile.createNewFile()
            configurationFile.writeText("{}")
            configurations = hashMapOf()
        } else {
            val reader = configurationFile.reader()
            configurations = try {
                val type = object : TypeToken<Map<Long, Configuration>>() {}.type
                gson.fromJson(reader, type)
            } catch (e: Exception) {
                Plugin.logger.warning("Error while parsing role file: ", e)
                hashMapOf()
            } finally {
                reader.close()
            }
        }
    }

    fun sync() {
        if (!currentFile.exists()) {
            currentFile.createNewFile()
        }

        if (isRecordsChanged) {
            try {
                val json = JsonObject()
                records.forEach { entry ->
                    val array = JsonArray()
                    entry.value.forEach { array.add(gson.toJsonTree(it, Record::class.java)) }
                    json.add(entry.key.toString(), array)
                }
                currentFile.writeText(gson.toJson(json))
                isRecordsChanged = false
            } catch (e: Exception) {
                Plugin.logger.warning("Failed to sync records: ", e)
            }
        }

        if (isConfigurationsChanged) {
            try {
                configurationFile.writeText(gson.toJson(configurations))
                isConfigurationsChanged = false
            } catch (e: Exception) {
                Plugin.logger.warning("Failed to sync roles: ", e)
            }
        }
    }

    operator fun get(group: Group): List<Record> = get(group.id)

    operator fun get(groupID: Long): List<Record> = records[groupID]?.toList() ?: arrayListOf()

    fun classes() = records.keys

    fun record(groupID: Long, instance: Record) {
        val r = records[groupID] ?: arrayListOf<Record>().also { records[groupID] = it }
        r.add(instance)
        isRecordsChanged = true
    }

    fun record(group: Group, instance: Record) = record(group.id, instance)

    fun unrecord(groupID: Long, filter: (Record) -> Boolean): Boolean {
        val r = records[groupID] ?: return false
        return r.removeIf(filter).also { if (it) isRecordsChanged = true }
    }

    fun unrecord(group: Group, filter: (Record) -> Boolean): Boolean = unrecord(group.id, filter)

    private fun getConfiguration(groupID: Long) =
        configurations[groupID] ?: Configuration().also { configurations[groupID] = it }

    fun markAsAdmin(target: Member): Boolean {
        val admins = getConfiguration(target.group.id).admins
        return if (!admins.contains(target.id)) {
            admins.add(target.id)
            isConfigurationsChanged = true
            true
        } else {
            false
        }
    }

    fun undoAdmin(target: Member): Boolean {
        val admins = getConfiguration(target.group.id).admins
        return if (admins.contains(target.id)) {
            admins.remove(target.id)
            isConfigurationsChanged = true
            true
        } else {
            false
        }
    }

    fun markAsClassmates(list: List<Member>) {
        if (list.isEmpty()) {
            return
        }
        val config = getConfiguration(list.first().group.id)
        list.forEach {
            if (!config.classmates.contains(it.id)) {
                config.classmates.add(it.id)
            }
        }
        isConfigurationsChanged = true
    }

    fun removeClassmates(list: List<Member>) {
        if (list.isEmpty()) {
            return
        }
        val config = getConfiguration(list.first().group.id)
        list.forEach {
            config.classmates.remove(it.id)
        }
        isConfigurationsChanged = true
    }

    fun classmates(groupID: Long) = getConfiguration(groupID).classmates.toList()

    fun classmates(group: Group) = Database.classmates(group.id)

    fun CommandSender.isOp(group: Group? = null): Boolean {
        return this.hasPermission(Plugin.parentPermission)
                || (group == null && this is MemberCommandSender && configurations[this.group.id]?.admins?.contains(user.id) == true)
                || (group != null && configurations[group.id]?.admins?.contains(user!!.id) == true)
    }
}