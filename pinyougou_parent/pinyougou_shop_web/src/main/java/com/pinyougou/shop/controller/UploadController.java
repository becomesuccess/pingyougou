package com.pinyougou.shop.controller;


import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String file_server_url;

    @RequestMapping("/upload")
    public Result  upload(MultipartFile file){

        //获取文件的全名称
        String filename = file.getOriginalFilename();
        //获取文件的扩展名称
        String ext = filename.substring(filename.lastIndexOf(".")+1);

        try {
            //获取客户端服务
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:config/fdfs_client.conf");
            //上传文件
            String s = fastDFSClient.uploadFile(file.getBytes(), ext);
            //拼接访问文件的地址
            String url =file_server_url+s;
            //上传成功返回
            return new Result(true,url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"上传失败");
        }
    }


}
