<template>
  <!-- 文件管理 -->
  <p class="title" style="font-size: 22px">ES索引区</p>
  <el-row class="card">
    <el-col :span="12">
      <div class="grid-content bg-purple-dark">
        <p class="title">导出JSON信息并添加文档(PUT)ES索引</p>
        <p class="title">[导入内容使用，将自动创建'blog'索引]</p>
        <el-button type="success" @click="doPut">PUT添加文档</el-button>
      </div>
    </el-col>
    <el-col :span="12">
      <div class="grid-content bg-purple-dark">
        <p class="title">目前的解决方案：数据库内容进行更新后ES内容也会更新</p>
        <p class="title">
          左边PUT是一次性导入数据至ES，ES数据出错则点击下方按钮
        </p>
        <el-button type="success" @click="doDelete">删除索引</el-button>
      </div>
    </el-col>
  </el-row>

  <p class="title" style="font-size: 22px; margin-top: 50px">
    博客文章图片上传区（预览地址：http://服务器IP地址:8001/resources/img/...）
  </p>
  <el-row class="card">
    <el-col :span="24">
      <div class="grid-content bg-purple-dark">
        <p class="title">文章分类图片（ClassificationImg）资源上传接口</p>
        <el-upload
          class="upload-demo"
          drag
          name="files"
          :action=uploadClassificationImgUrl
          multiple>
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              单个文件上传不超过 800MB，多个文件上传不超过 5GB
            </div>
          </template>
        </el-upload>
      </div>
    </el-col>
  </el-row>
    <el-row class="card">
    <el-col :span="24">
      <div class="grid-content bg-purple-dark">
        <p class="title">文章内容的图片（ContentImg/Base）资源上传接口</p>
        <el-upload
          class="upload-demo"
          drag
          name="files"
          :action=uploadContentImgUrl
          multiple>
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              单个文件上传不超过 800MB，多个文件上传不超过 5GB
            </div>
          </template>
        </el-upload>
      </div>
    </el-col>
  </el-row>
    <el-row class="card">
    <el-col :span="24">
      <div class="grid-content bg-purple-dark">
        <p class="title">文章封面图片（PageImg）资源上传接口</p>
        <el-upload
          class="upload-demo"
          drag
          name="files"
          :action=uploadPageImgUrl
          multiple>
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              单个文件上传不超过 800MB，多个文件上传不超过 5GB
            </div>
          </template>
        </el-upload>
      </div>
    </el-col>
  </el-row>
</template>

<script>
import { mixin } from "@/mixins/index";
import { putES, deleteES } from "@/api/index";

export default {
  mixins: [mixin],
  data(){
    return{
      //写成自己的服务器接口，后台是在 com.chen.common.upload.main.java 中实现，
      // http://服务器IP:端口号/main/... 这个路径的API只有root用户能够访问
      uploadClassificationImgUrl: '/api/main/uploadClassificationImg/',
      uploadContentImgUrl: '/api/main/uploadContentImg/',
      uploadPageImgUrl: '/api/main/uploadPageImg/'
    }
  },
  methods: {
    doPut() {
      //添加文档至索引，后台回自动创建名为 "blog"的索引
      let _this = this;
      putES().then((response) => {
        if (response.code === 20000) {
          _this.notify("添加成功", "success");
        } else {
          _this.notify("添加失败", "error");
        }
      });
    },
    doDelete() {
      //删除索引-"blog"
      let _this = this;
      deleteES().then((response) => {
        if (response.code === 20000) {
          _this.notify("删除成功", "success");
        } else {
          _this.notify("删除失败", "error");
        }
      });
    }
  },
};
</script>

<style scoped>
.card {
  margin: 10px;
  border: 2px solid black;
  padding: 20px;
}
.title {
  margin-bottom: 20px;
}
.grid-content {
  height: auto;
  text-align: center;
}
</style>
