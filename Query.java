import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import redis.clients.jedis.Jedis;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/query")
public class Query extends HttpServlet {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String URL =
            "jdbc:mysql://106.12.175.36/linux_final?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
    static final String USER = "root";
    static final String PASS = "Ahchc0419";
    static final String SQL_QUERY_NOTEPAD = "SELECT * FROM t_student";

    static Connection conn = null;
    static Jedis jedis = null;

    // servlet创建时 初始化的东西
    public void init() {
        try {
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASS);
            jedis = new Jedis("106.12.175.36");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 当再次调用servlet销毁之前
    public void destroy() {
        try {
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 重写doGet方法
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // response 返回值类型
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        // response 返回
        PrintWriter out = response.getWriter();

        // 调用业务方法得到返回值

        String studentList = jedis.get("studentlist");

        if (studentList == null) {
            List<Student> list = getStudent();
            Gson gson = new Gson();
            String json = gson.toJson(list, new TypeToken<List<Student>>() {
            }.getType());
            jedis.set("studentlist", json);
            out.println(json);
            System.out.println("走数据库，存入缓存");
        } else {
            out.println(studentList);
            System.out.println("走缓存");
        }

        out.flush();
        out.close();

    }

    // 查询 返回一个泛型为实体类的List集合
    private List<Student> getStudent() {
        List<Student> list = new ArrayList<Student>();
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(SQL_QUERY_NOTEPAD);
            while (rs.next()) {
                Student student = new Student();
                student.setId(rs.getString("id"));
                student.setName(rs.getString("name"));
                student.setAge(rs.getString("age"));
                student.setSex(rs.getString("sex"));
                list.add(student);
            }
            rs.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return list;
    }

    class Student {
        private String id;
        private String name;
        private String age;
        private String sex;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAge() {
            return age;
        }

        public void setAge(String age) {
            this.age = age;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }



    }
}
