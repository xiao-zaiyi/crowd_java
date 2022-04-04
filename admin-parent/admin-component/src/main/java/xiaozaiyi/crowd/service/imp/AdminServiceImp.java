package xiaozaiyi.crowd.service.imp;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xiaozaiyi.crowd.entity.Admin;
import xiaozaiyi.crowd.entity.AdminExample;
import xiaozaiyi.crowd.exception.CustomException;
import xiaozaiyi.crowd.mapper.AdminMapper;
import xiaozaiyi.crowd.service.AdminService;
import xiaozaiyi.crowd.util.CrowedUtils;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Objects;


/**
 * @author : Crazy_August
 * @description :
 * @Time: 2022-03-30   14:26
 */
@Service
public class AdminServiceImp implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    private Logger logger = LoggerFactory.getLogger(AdminServiceImp.class);

    public void saveAdmin(Admin admin) {
        adminMapper.insert(admin);
    }

    public Admin getAdminByLoginAcct(String loginAcct, String userPswd) {
        // 1. 判断用户名是否合法
        if (loginAcct == null || loginAcct.trim().length() == 0) {
            return null;
        }
        // 2.判断对象是否为null
        AdminExample adminExample = new AdminExample();
        AdminExample.Criteria criteria = adminExample.createCriteria();
        // 2.1在 criteria封装查询条件
        criteria.andLoginAcctEqualTo(loginAcct);
        // 2.2 调用 AdminMapper 方法执行
        List<Admin> admins = adminMapper.selectByExample(adminExample);
        if (admins == null || admins.size() == 0) {
            throw new CustomException(401, "用户名不存在");
        }
        // 3. 如果对象不为null,则将数据库密码取出
        if (admins.size() > 1) {
            throw new CustomException(401, "存在多个相同账号系统异常");
        }
        Admin admin = admins.get(0);
        // 4.将表单进行加密后比较
        String password = admin.getUserPswd().toUpperCase();
        String newUserPswd = Objects.requireNonNull(CrowedUtils.md5(userPswd)).toUpperCase();
        logger.info(password);
        logger.info(newUserPswd);
        if (!Objects.equals(password, newUserPswd)) {
            throw new CustomException(401, "密码不正确");
        }

        // 5.返回结果
        return admin;
    }

    @Override
    public PageInfo<Admin> getPageInfo(String keyWord, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Admin> admins = adminMapper.selectAdminByKeyWord(keyWord);
        return new PageInfo<>(admins);
    }

    @Override
    public int deleteAdmin(Integer id) {
        return adminMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int updateAdmin(Admin admin) {
        String nwePassword = Objects.requireNonNull(CrowedUtils.md5(admin.getUserPswd())).toUpperCase();
        admin.setUserPswd(nwePassword);
        return adminMapper.updateByPrimaryKeySelective(admin);
    }

    @Override
    public int addAdmin(Admin admin) {
        String nwePassword = Objects.requireNonNull(CrowedUtils.md5(admin.getUserPswd())).toUpperCase();
        admin.setUserPswd(nwePassword);
        // 创建时间
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String createTime = simpleDateFormat.format(date);
        admin.setCreateTime(createTime);
        return adminMapper.insert(admin);
    }

    @Override
    public int batchDeleteByIds(List<Integer> ids) {
        return adminMapper.batchDeleteByIds(ids);
    }
}