package box

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.SequenceInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collections
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//可以设置文件在读取时一次读取文件的大小　  
val length: Int = 128*1024;

/* 
* 文件的切割 
* String path   切割文件的路径 
* size 　　　　 子文件的大小 
* */
public class MyException(override val message: String) : Throwable(){

}


public fun filesplit(path: String){
	if(path.length ==0)
		throw MyException("路径不能为空!!!")
  val file: File = File(path)
	if(!file.exists())  
		throw MyException("文件不存在!!!");
	val reader = FileInputStream(file)
	val buffer: ByteArray = ByteArray(length)
	var i: Int = 0
	var rcnt: Int = 0
  var checksum: Checksum = Adler32();
  var str = ""
	while({rcnt = reader.read(buffer); rcnt}()!= -1){
    checksum.update(buffer, 0, rcnt)
    val checksumSigle: Checksum = Adler32();
		checksumSigle.update(buffer, 0, rcnt);
    str += "||" + checksumSigle.getValue() + "|" + rcnt + "\n"
		val writer = FileOutputStream(file.getAbsolutePath() + ".patch." + i)
		writer.write(buffer, 0, rcnt)
		i++
	}
  val absolutePath = file.getAbsolutePath()
  val filePath: String = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator))
  val wfile: File = File(filePath + "/test.txt");
  str = "|" + file.getName() + "|" + checksum.getValue() +"|" + file.length() + "|\n" + str
	val fos: FileOutputStream  = FileOutputStream(wfile);
	val osw: OutputStreamWriter = OutputStreamWriter(fos);
  osw.write(str)
  osw.close()
  val tReader = FileInputStream(wfile)
  val checksumTotal: Checksum = Adler32();
	val tBuffer: ByteArray = ByteArray(wfile.length().toInt())
	checksumTotal.update(tBuffer, 0, tReader.read(tBuffer));
  val newSource: File = File(filePath + "/" +  checksumTotal.getValue() + ".txt");
	if(newSource.exists())  
		throw MyException("文件已存在!!!");
  Files.move(wfile.toPath(), newSource.toPath());
  println(checksum.getValue())
}

fun getNum(fileName: String): Int{
   val lastIndexOfDot: Int = fileName.lastIndexOf(".")
   val fileNameLength: Int = fileName.length
   val extension: String = fileName.substring(lastIndexOfDot+1, fileNameLength)
   return extension.toInt()
}

public fun FileMerge(path: String, filename: String) {
    /**
     * 需求：使用SequenceInputStream类来合并碎片文件
     * 1.创建一个list集合,来保存指定文件夹碎片流集合
     * 2.用集合工具类方法Collections.enumeration()方法将list集合转换为Enumeration
     * 3.新建一个SequenceInputStream流对象，并传入第2步的Enumeration
     * 4.创建一个输出流对象，创建缓冲区循环写第3步SequenceInputStream读取的内容
     */
    val partDir: File = File(path)
    val listfile: Array<File> = partDir.listFiles()
    val new_list: Array<File> = listfile.filter{it.getName().contains("patch")}.toTypedArray()
		val count = new_list.size
    new_list.sortBy({getNum(it.getName())})        //根据文件名进行排序
   	val list: ArrayList<FileInputStream> = ArrayList<FileInputStream>()
    for (i in 0..count-1){
      println(new_list.get(i).getName())
      val fis: FileInputStream = FileInputStream(File(partDir, new_list.get(i).getName()))
      list.add(fis)
    }
    val en = Collections.enumeration(list)
    val sis = SequenceInputStream(en)
 
    val fos: FileOutputStream = FileOutputStream(File(partDir, filename))

    val buf: ByteArray = ByteArray(128*1024)
    var len: Int = 0;
    while ({len = sis.read(buf); len}() != -1){
      fos.write(buf, 0, len);
    }
    fos.close();
    sis.close();
}


public fun main(args: Array<String>){  
	// val path: String = """/dev/shm/1.2.0.apk"""
	val path: String = """/dev/shm/"""
	// filesplit(path);
	val filename: String = "tkzc.apk"
	FileMerge(path, filename)
}

