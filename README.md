# CodeHelper

Eclipse代码自动生成插件，目前已支持转换、覆盖、填充方法的自动生成。

# 编译和安装

1. 使用Eclipse Plugin-in project导入编译，或者直接下载已编译的[release版本](https://github.com/fengwk/code-helper/releases/tag/v1.0)。
2. 将编译后的jar包放在Eclipse目录下的plugins文件夹里。
3. 重启Eclipse，需注意的是重启并不一定能使插件生效，如不生效新建一个工作空间即可。

# 功能与使用

所有的功能集成在右键菜单的Code Helper项中，可自行查看，下面将逐个演示每项功能的用途。

定义Class：

```java
public class StudentDO {
    private Long id;
    private String name;
    private Integer sex;
    private Integer age;
    /* 省略getter、setter方法 */
}

public class StudentBO {
    private Long id;
    private String name;
    private Integer sex;
    private Integer age;
    /* 省略getter、setter方法 */
}
```

## Gen Convert Method

Gen Convert Method用于生成对象转换方法，将入参对象转换为出参对象。

1. 首先定义转换方法框架。

```java
public static StudentBO convert(StudentDO studentDO) {
        
}
```

2. 将光标定位到方法体内部，或者选中方法名称，右键打开菜单，选择Code Helper -> Gen Convert Method，将自动生成如下代码。

```java
public static StudentBO convert(StudentDO studentDO) {
    if (studentDO == null) {
        return null;
    }
    StudentBO studentBO = new StudentBO();
    studentBO.setId(studentDO.getId());
    studentBO.setName(studentDO.getName());
    studentBO.setSex(studentDO.getSex());
    studentBO.setAge(studentDO.getAge());
    return studentBO;
}
```

## Gen Cover Method

Gen Cover Method用于生成对象覆盖方法，使用入参[0]覆盖入参[1]。

1. 首先定义覆盖方法框架。

```java
public static void cover(StudentDO studentDO, StudentBO studentBO) {

}
```

2. 将光标定位到方法体内部，或者选中方法名称，右键打开菜单，选择Code Helper -> Gen Cover Method，将自动生成如下代码。

```java
public static void cover(StudentDO studentDO, StudentBO studentBO) {
    if (studentDO == null || studentBO == null) {
        return;
    }
    studentBO.setId(studentDO.getId());
    studentBO.setName(studentDO.getName());
    studentBO.setSex(studentDO.getSex());
    studentBO.setAge(studentDO.getAge());
}
```

## Gen Cover Method (if from not null)

与Gen Cover Method类似，添加了入参[0]非空判断，下面是生成的代码。

```java
public static void cover(StudentDO studentDO, StudentBO studentBO) {
    if (studentDO == null || studentBO == null) {
        return;
    }
    if (studentDO.getId() != null) {
        studentBO.setId(studentDO.getId());
    }
    if (studentDO.getName() != null) {
        studentBO.setName(studentDO.getName());
    }
    if (studentDO.getSex() != null) {
        studentBO.setSex(studentDO.getSex());
    }
    if (studentDO.getAge() != null) {
        studentBO.setAge(studentDO.getAge());
    }
}
```

## Gen Cover Method (if to is null)

与Gen Cover Method类似，添加了入参[1]为空判断，下面是生成的代码。

```java
public static void cover(StudentDO studentDO, StudentBO studentBO) {
    if (studentDO == null || studentBO == null) {
        return;
    }
    if (studentBO.getAge() == null) {
        studentBO.setAge(studentDO.getAge());
    }
    if (studentBO.getId() == null) {
        studentBO.setId(studentDO.getId());
    }
    if (studentBO.getName() == null) {
        studentBO.setName(studentDO.getName());
    }
    if (studentBO.getSex() == null) {
        studentBO.setSex(studentDO.getSex());
    }
}
```

## Gen Auto Set

Gen Auto Set用于自动填充对象属性，这在编写测试用例时将非常有用。

1. 右键打开菜单，选择Code Helper -> Gen Auto Set，将弹出Type Selector面板。
2. 在Type Selector面板里选择要自动填充的对象，可以多选。
3. 例如选择了StudentDO和StudentBO插件将自动为你生成如下代码。

```java
StudentDO studentDO = new StudentDO();
studentDO.setId(0L);
studentDO.setName("name");
studentDO.setSex(0);
studentDO.setAge(0);
StudentBO studentBO = new StudentBO();
studentBO.setId(0L);
studentBO.setName("name");
studentBO.setSex(0);
studentBO.setAge(0);
```
