package com.iget.apt.poc.controller.admin;

import com.github.pagehelper.PageInfo;
import com.iget.apt.poc.constant.WebConst;
import com.iget.apt.poc.controller.BaseController;
import com.iget.apt.poc.dto.LogActions;
import com.iget.apt.poc.dto.Types;
import com.iget.apt.poc.exception.TipException;
import com.iget.apt.poc.modal.Bo.RestResponseBo;
import com.iget.apt.poc.modal.Vo.AttachVo;
import com.iget.apt.poc.modal.Vo.UserVo;
import com.iget.apt.poc.service.IAttachService;
import com.iget.apt.poc.service.ILogService;
import com.iget.apt.poc.utils.AdminCommons;
import com.iget.apt.poc.utils.Commons;
import com.iget.apt.poc.utils.TaleUtils;
import com.iget.apt.poc.utils.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 附件管理
 *
 * Created by 13 on 2017/2/21.
 */
@Controller
@RequestMapping("admin/attach")
public class AttachController extends BaseController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AttachController.class);

    public static final String CLASSPATH = TaleUtils.getUplodFilePath();

    @Resource
    private IAttachService attachService;

    @Resource
    private ILogService logService;

    /**
     * 附件页面
     *
     * @param request
     *
     * @param page
     * @param limit
     * @return
     */
    @GetMapping(value = "")
    public String index(HttpServletRequest request, @RequestParam(value = "page", defaultValue = "1") int page,
                        @RequestParam(value = "limit", defaultValue = "12") int limit) {
        PageInfo<AttachVo> attachPaginator = attachService.getAttachs(page, limit);
        request.setAttribute("attachs", attachPaginator);
        request.setAttribute(Types.ATTACH_URL.getType(), Commons.site_option(Types.ATTACH_URL.getType(), Commons.site_url()));
        request.setAttribute("max_file_size", WebConst.MAX_FILE_SIZE / 1024);
        return "admin/attach";
    }

    /**
     * 上传文件接口
     *
     * @param request
     * @return
     */
    @PostMapping(value = "upload")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo upload(HttpServletRequest request, @RequestParam("file") MultipartFile[] multipartFiles) throws IOException {
        Integer uid = AdminCommons.getAdminID();
        List<String> errorFiles = new ArrayList<>();
        try {
            for (MultipartFile multipartFile : multipartFiles) {
                String fname = multipartFile.getOriginalFilename();
                if (multipartFile.getSize() <= WebConst.MAX_FILE_SIZE) {
                    String fkey = TaleUtils.getFileKey(fname);
                    String ftype = TaleUtils.isImage(multipartFile.getInputStream()) ? Types.IMAGE.getType() : Types.FILE.getType();
                    File file = new File(CLASSPATH + fkey);
                    try {
                        FileCopyUtils.copy(multipartFile.getInputStream(),new FileOutputStream(file));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    attachService.save(fname, fkey, ftype, uid);
                } else {
                    errorFiles.add(fname);
                }
            }
        } catch (Exception e) {
            return RestResponseBo.fail();
        }
        return RestResponseBo.ok(errorFiles);
    }


    /**
     * 文件下载
     * @param id
     */
    @RequestMapping("/download")
    public void downloadFile(Integer id, HttpServletResponse response) throws Exception {
        AttachVo attachVo = attachService.selectById(id);
        String fname = attachVo.getFname();
        String fkey = attachVo.getFkey();
        LOGGER.debug("当前下载的文件名是：{}", fname);
        LOGGER.debug("当前下载的文件的目录是：{}", CLASSPATH);
        // 1.去指定目录读取文件
        File file = new File(CLASSPATH + fkey);
        // 2.将文件读取为文件输入流
        FileInputStream is = new FileInputStream(file);

        // 2.1 获取响应流之前  一定要设置以附件形式下载
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fname, "UTF-8"));
        // 3.获取响应输出流
        ServletOutputStream os = response.getOutputStream();
        // 4.输入流复制给输出流

        /*int len = 0;
        byte[] buff = new byte[1024];
        while (true) {
            len = is.read(buff);
            if (len==-1) break;
            os.write(buff, 0, len);
        }*/

        // 5.释放资源
        /*os.close();
        is.close();*/

        FileCopyUtils.copy(is,os);

    }

    /**
     * 删除附件
     * @param id
     * @param request
     * @return
     */
    @RequestMapping(value = "delete")
    @ResponseBody
    @Transactional(rollbackFor = TipException.class)
    public RestResponseBo delete(@RequestParam Integer id, HttpServletRequest request) {
        try {
            AttachVo attach = attachService.selectById(id);
            if (null == attach) return RestResponseBo.fail("不存在该附件");
            attachService.deleteById(id);
            new File(CLASSPATH+attach.getFkey()).delete();
            logService.insertLog(LogActions.DEL_ARTICLE.getAction(), attach.getFkey(), request.getRemoteAddr(), this.getUid(request));
        } catch (Exception e) {
            String msg = "附件删除失败";
            if (e instanceof TipException) msg = e.getMessage();
            else LOGGER.error(msg, e);
            return RestResponseBo.fail(msg);
        }
        return RestResponseBo.ok();
    }

}
