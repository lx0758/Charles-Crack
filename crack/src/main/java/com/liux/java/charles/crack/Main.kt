@file:JvmName("Main")

package com.liux.java.charles.crack

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AccessFlag
import javassist.bytecode.MethodInfo
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import javax.swing.filechooser.FileFilter


fun main(args: Array<String>) {
    try {
        val targetFile = obtainFile()
        val targetUserName = obtainUserName()

        crackJar(targetFile, targetUserName)

        showMessage("操作完成!")
    } catch (e: Exception) {
        showMessage(e.message ?: "未知故障")
        e.printStackTrace()
    }
}

fun obtainFile(): File {
    val chooser = JFileChooser().apply {
        dialogTitle = "选择目标文件"
        fileFilter = object : FileFilter() {
            override fun accept(file: File?): Boolean {
                if (file == null) return false
                if (file.isDirectory) return true
                return file.name == description
            }

            override fun getDescription(): String {
                return "charles.jar"
            }
        }
        fileSelectionMode = JFileChooser.FILES_ONLY
        isFileHidingEnabled = false
    }
    val state = chooser.showOpenDialog(null)
    if (state == JFileChooser.ERROR_OPTION) throw IllegalStateException("选取错误!")
    if (state == JFileChooser.CANCEL_OPTION) throw IllegalStateException("取消选择!")
    return chooser.selectedFile
}

fun obtainUserName(): String {
    var userName: String = JOptionPane.showInputDialog("请输入自定义的用户名(默认:Admin):") ?: throw NullPointerException("取消操作")
    if (userName.isEmpty()) userName = "Admin"
    return userName
}

fun crackJar(file: File, userName: String) {
    val readOnlyTempFile = File.createTempFile("charles-", ".jar")
    FileUtils.copyFile(file, readOnlyTempFile)

    val targetClassPool = ClassPool .getDefault().apply { insertClassPath(readOnlyTempFile.path) }
    val jarFile = JarFile(readOnlyTempFile)



    var targetClass: CtClass? = null
    var targetConditionMethod: CtMethod? = null
    var targetUserNameMethod: CtMethod? = null

    for (jarEntry in jarFile.entries()) {
        if (!jarEntry.name.matches(Regex("com/xk72/charles/[0-9a-zA-Z]{4}\\.class"))) continue

        val className = jarEntry.name.replace(".class", "").replace('/', '.')
        val ctClass = targetClassPool.get(className)

        var conditionMethod: CtMethod? = null
        var userNameMethod: CtMethod? = null
        for (ctMethod in ctClass.methods) {
            if (!ctMethod.name.matches(Regex("[a-zA-Z]{4}"))) continue
            if (!ctMethod.methodInfo.hasAccessFlags(AccessFlag.PUBLIC, AccessFlag.STATIC)) continue
            when(ctMethod.signature) {
                "()Z" -> conditionMethod = ctMethod
                "()Ljava/lang/String;" -> userNameMethod = ctMethod
            }
        }
        if (conditionMethod == null || userNameMethod == null) continue

        targetClass = ctClass
        targetConditionMethod = conditionMethod
        targetUserNameMethod = userNameMethod
        break
    }

    if (targetClass == null || targetConditionMethod == null || targetUserNameMethod == null) throw NullPointerException("查找特征失败")



    targetConditionMethod.setBody("{return true;}")
    targetUserNameMethod.setBody("{return \"$userName\";}")
    val newClassBytes = targetClass.toBytecode()

    val tempFile = File(file.path + ".tmp")
    val jarOutputStream = JarOutputStream(FileOutputStream(tempFile))

    val jarEntryName = targetClass.name.replace('.', '/') + ".class"
    for (jarEntry in jarFile.entries()) {
        if (jarEntry.name == jarEntryName) {
            jarOutputStream.putNextEntry(JarEntry(jarEntry.name))
            jarOutputStream.write(newClassBytes)
        } else {
            val newJarEntry = JarEntry(jarEntry.name)
            newJarEntry.method = jarEntry.method
            newJarEntry.time = jarEntry.time
            newJarEntry.comment = jarEntry.comment
            newJarEntry.extra = jarEntry.extra
            if (jarEntry.method == ZipEntry.STORED) {
                newJarEntry.size = jarEntry.size
                newJarEntry.crc = jarEntry.crc
            }
            jarOutputStream.putNextEntry(newJarEntry)
            val jarEntryInputStream = jarFile.getInputStream(jarEntry)
            jarOutputStream.write(IOUtils.toByteArray(jarEntryInputStream))
            jarEntryInputStream.close()
        }
    }

    jarOutputStream.close()

    jarFile.close()

    try {
        FileUtils.forceDelete(file)
    } catch (e: Exception) {
        tempFile.delete()
        throw e
    }
    FileUtils.moveFile(tempFile, file)
    readOnlyTempFile.deleteOnExit()
}

fun MethodInfo.hasAccessFlags(vararg flags: Int):Boolean {
    for (flag in flags) {
        if (accessFlags and flag != flag) return false
    }
    return true
}

fun showMessage(message: String) {
    JOptionPane.showMessageDialog(null, message)
}
