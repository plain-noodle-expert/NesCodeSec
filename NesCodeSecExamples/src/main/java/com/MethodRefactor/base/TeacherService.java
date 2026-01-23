package dev.dubhe.libms.service;

import com.alibaba.fastjson2.JSONObject;
import dev.dubhe.libms.dao.ITeacherDao;
import dev.dubhe.libms.mapper.domain.Teacher;
import dev.dubhe.libms.utils.WrapperUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class TeacherService {
    private final ITeacherDao teacherDao;


    public JSONObject login(Long uid, String password) {
        Teacher teacher = this.teacherDao.getOne(WrapperUtil.eq("tno", uid));
        if (teacher == null || !teacher.getTpassword().equals(password)) return null;
        int token = teacher.getTpassword().hashCode();
        JSONObject object = new JSONObject();
        object.put("token", String.valueOf(token));
        object.put("username", teacher.getTname());
        return object;
    }

    public int pwd(Long uid, String password) {
        Teacher teacher = this.teacherDao.getOne(WrapperUtil.eq("tno", uid));
        if (teacher != null) {
            teacher.setTpassword(password);
            this.teacherDao.updateById(teacher);
            return password.hashCode();
        }
        return -1;
    }

    @SuppressWarnings("all")
    public boolean verify(Long uid, int hash) {
        Teacher teacher = this.teacherDao.getOne(WrapperUtil.eq("tno", uid));
        if (teacher != null) {
            return teacher.getTpassword().hashCode() == hash;
        }
        return false;
    }
}