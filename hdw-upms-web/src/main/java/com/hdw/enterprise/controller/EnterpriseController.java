package com.hdw.enterprise.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hdw.common.base.BaseController;
import com.hdw.common.result.PageInfo;
import com.hdw.common.result.Select2Node;
import com.hdw.common.result.ZTreeNode;
import com.hdw.enterprise.entity.Enterprise;
import com.hdw.enterprise.service.IEnterpriseService;
import com.hdw.upms.service.ISysDicService;
import com.hdw.upms.shiro.ShiroKit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.util.*;

/**
 * @author WangRui
 * @description 企业信息 前端控制器
 * @date 2018年5月4日
 */

@Api(value = " 企业信息类", tags = {" 企业信息接口"})
@Controller
@RequestMapping("/enterprise")
public class EnterpriseController extends BaseController {

    @Reference(version = "1.0.0", application = "${dubbo.application.id}", url = "dubbo://localhost:20881")
    private IEnterpriseService enterpriseService;

	@Reference(version = "1.0.0", application = "${dubbo.application.id}", url = "dubbo://localhost:20880")
	private ISysDicService sysDicService;

	/**
	 *  管理页
	 * @return
	 */
	@RequestMapping("/manager")
	public String manager() {
		return "enterprise/enterprise";
	}

	/**
	 * 获取企业列表
	 * @param offset
	 * @param limit
	 * @param sort
	 * @param order
	 * @param enterpriseId
	 * @param enterpriseName
	 * @param industryCode
	 * @param areaCode
	 * @return
	 */
    @ApiOperation(value = "获取企业列表", notes = "获取企业列表")
	@ApiImplicitParams({ @ApiImplicitParam(name = "offset", value = "页数", dataType = "int", required = true),
			@ApiImplicitParam(name = "limit", value = "行数", dataType = "int", required = true),
			@ApiImplicitParam(name = "sort", value = "根据某属性排序", dataType = "string"),
			@ApiImplicitParam(name = "order", value = "升降序", dataType = "string"),
			@ApiImplicitParam(name = "enterpriseId", value = "企业Id", dataType = "int"),
			@ApiImplicitParam(name = "enterpriseName", value = "企业名称", dataType = "string"),
	        @ApiImplicitParam(name = "industryCode", value = "行业Id", dataType = "string"),
	        @ApiImplicitParam(name = "areaCode", value = "区域Id", dataType = "string")})
	@RequestMapping("/dataGrid")
	@ResponseBody
	public Object dataGrid(Integer offset, Integer limit, String sort, String order, 
			Long enterpriseId,String enterpriseName, String industryCode, String areaCode) {
        PageInfo pageInfo = new PageInfo(offset, limit, sort, order);
        Map<String, Object> condition = new HashMap<String, Object>();
        if (enterpriseId!=null) {
            condition.put("enterpriseId", enterpriseId);
        }
        if (StringUtils.isNotBlank(enterpriseName)) {
            condition.put("enterpriseName", enterpriseName);
        }
        if (StringUtils.isNotBlank(industryCode)) {
            condition.put("industryCode", industryCode);
        }
        if (StringUtils.isNotBlank(areaCode)) {
            condition.put("areaCode", areaCode);
        }
        pageInfo.setCondition(condition);
        PageInfo page=enterpriseService.selectDataGrid(pageInfo);
        return page;
    }


	/**
	 * 根据行业Id或者区域Id获取企业select2树
	 *
	 * @return
	 */
	@ApiOperation(value = "根据行业Id或者区域Id获取企业select2树", notes = "根据行业Id或者区域Id获取企业select2树")
	@RequestMapping("/select2Tree")
	@ResponseBody
	public Object select2Tree(String areaCode,String industryCode) {
		List<Select2Node> s2tList=new ArrayList<>();
		Map<String,Object> par=new HashMap<>();
		if(StringUtils.isNotBlank(areaCode)){
			par.put("areaCode",areaCode);
		}
		if(StringUtils.isNotBlank(industryCode)){
			par.put("industryCode",industryCode);
		}
		List<ZTreeNode> trees = enterpriseService.selectTree(par);
		if(trees!=null&&!trees.isEmpty()) {
			for (ZTreeNode zn : trees) {
				Select2Node s2n = new Select2Node();
				s2n.setId(zn.getId());
				s2n.setText(zn.getName());
				s2tList.add(s2n);
			}
		}
		return s2tList;
	}

    /**
	 * 根据行业Id或者区域Id获取企业树
	 *
	 * @return
	 */
	@ApiOperation(value = "根据行业Id或者区域Id获取企业树", notes = "根据行业Id或者区域Id获取企业树")
	@PostMapping("/tree")
	@ResponseBody
	public List<ZTreeNode> tree(String areaCode,String industryCode) {
		Map<String,Object> par=new HashMap<>();
		if(StringUtils.isNotBlank(areaCode)){
			par.put("areaCode",areaCode);
		}
		if(StringUtils.isNotBlank(industryCode)){
			par.put("industryCode",industryCode);
		}
		List<ZTreeNode> trees = enterpriseService.selectTree(par);
		trees.add(ZTreeNode.createParent());
		return trees;
	}

	/**
	 * 根据区域父Id获取企业树
	 *
	 * @return
	 */
	@ApiOperation(value = "根据区域父Id获取企业树", notes = "根据区域父Id获取企业树")
	@PostMapping("/selectTreeByAreaCode/{areaCode}")
	@ResponseBody
	public List<ZTreeNode> selectTreeByAreaCodePid(@PathVariable("areaCodePid") String areaCodePid) {
		List<ZTreeNode> treeNodeList=new ArrayList<>();
		Map<String, Object> par = new HashMap<>();
		par.put("pid", areaCodePid);
		List<ZTreeNode> trees = sysDicService.selectTree(par);
		if(trees!=null && !trees.isEmpty()){
			for (ZTreeNode zn:trees){
				//设置区域父Id为0；
				zn.setpId(0l);
				par.clear();
				par.put("areaCode",zn.getId());
				List<ZTreeNode> trees2 = enterpriseService.selectTree(par);
				if(trees2!=null && trees2.isEmpty()){
					for (ZTreeNode zn2: trees2){
						zn2.setpId(zn.getId());
						zn2.setOpen(false);
						//添加企业
						treeNodeList.add(zn2);
						//当查询到区域下有企业，展开ztree
						zn.setOpen(true);
					}
				}else{
					//当查询到区域下有企业，不展开展开ztree
					zn.setOpen(false);
				}
                //添加行业
				treeNodeList.add(zn);
			}
		}
		return trees;
	}

	/**
	 * 根据行业父Id获取企业树
	 *
	 * @return
	 */
	@ApiOperation(value = "根据行业父Id获取企业树", notes = "根据行业父Id获取企业树")
	@PostMapping("/selectTreeByIndustryCodePid/{industryCodePid}")
	@ResponseBody
	public List<ZTreeNode> selectTreeByIndustryCode(@PathVariable("industryCodePid") String industryCodePid) {
		List<ZTreeNode> treeNodeList=new ArrayList<>();
		Map<String, Object> par = new HashMap<>();
		par.put("pid", industryCodePid);
		List<ZTreeNode> trees = sysDicService.selectTree(par);
		if(trees!=null && !trees.isEmpty()){
			for (ZTreeNode zn:trees){
				//设置区域父Id为0；
				zn.setpId(0l);
				par.clear();
				par.put("industryCode",zn.getId());
				List<ZTreeNode> trees2 = enterpriseService.selectTree(par);
				if(trees2!=null && trees2.isEmpty()){
					for (ZTreeNode zn2: trees2){
						zn2.setpId(zn.getId());
						zn2.setOpen(false);
						//添加企业
						treeNodeList.add(zn2);
						//当查询到区域下有企业，展开ztree
						zn.setOpen(true);
					}
				}else{
					//当查询到区域下有企业，不展开展开ztree
					zn.setOpen(false);
				}
				//添加行业
				treeNodeList.add(zn);
			}
		}
		return trees;
	}

	/**
	 * 添加企业页
	 * @return
	 */
	@RequestMapping("/addPage")
	public String addPage() {
		return "enterprise/enterpriseAdd";
	}


	@RequestMapping("/editPage/{enterpriseId}")
	public String editPage(Model model, @PathVariable("enterpriseId") Long enterpriseId) {
		Enterprise enterprise = enterpriseService.selectById(enterpriseId);
		model.addAttribute("enterprise", enterprise);
		return "enterprise/enterpriseEdit";
	}


	/**
	 * 编辑企业
	 * @param enterprise
	 * @return
	 * @throws RuntimeException
	 */
	@ApiOperation(value = "编辑企业", notes = "编辑企业")
	@RequestMapping("/edit")
	@ResponseBody
	public Object edit(@Valid Enterprise enterprise) throws RuntimeException {
		try {
			if (enterprise.getId() != null) {
				enterprise.setUpdateTime(new Date());
				enterprise.setUpdateUser(ShiroKit.getUser().getLoginName());
				enterpriseService.updateById(enterprise);
				return renderSuccess("编辑成功！");
			} else {
				Map<String,Object> par=new HashMap<>();
				par.put("enterpriseName",enterprise.getEnterpriseName());
				Enterprise enterprise2=enterpriseService.selectEnterpriseByMap(par);
				if(enterprise2!=null){
					return renderError("添加成功！");
				}else{
					enterprise.setCreateTime(new Date());
					enterprise.setUpdateTime(new Date());
					enterprise.setCreateUser(ShiroKit.getUser().getLoginName());
					enterprise.setUpdateUser(ShiroKit.getUser().getLoginName());
					enterpriseService.insert(enterprise);
					return renderSuccess("添加成功！");
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RuntimeException("运行异常，请联系管理员");
		}
	}

	/**
	 * 删除企业
	 * @param enterpriseId
	 * @return
	 */
	@ApiOperation(value = "删除企业", notes = "删除企业")
	@RequestMapping("/delete")
	@ResponseBody
	public Object delete(Long enterpriseId) throws  RuntimeException{
		try {
			enterpriseService.deleteById(enterpriseId);
			return renderSuccess("删除成功！");
		}catch (Exception e){
			logger.error(e.getMessage());
			throw new RuntimeException("删除失败，请联系管理员");
		}

	}
}
