package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import dto.BoardDTO;

public class BoardDAO {
	private static BoardDAO instance;
	
	public synchronized static BoardDAO getInstance(){
		if (instance == null) {
			instance = new BoardDAO();
		}
		return instance;
	}
	
	private Connection getconnection() throws Exception {
		Context ctx = new InitialContext(); 
		DataSource ds =  (DataSource)ctx.lookup("java:comp/env/jdbc/oracle");
		return ds.getConnection();
	}
	
	private BoardDAO(){};

	// 게시판 게시글 조회 
	public List<BoardDTO> selectListAll(int startNum, int endNum) throws Exception {
		// 내부 조인으로 desc 순으로 번호 출력
		System.out.println("board_seq");
		String sql = "select * from (select board.*, row_number() over(order by board_seq desc) rown from board) where rown between ? and ?";
		try (
				Connection con = this.getconnection();
				PreparedStatement pstat = con.prepareStatement(sql);	
				) {
			List<BoardDTO> list = new ArrayList<>();
			pstat.setInt(1, startNum);
			pstat.setInt(2, endNum);
			try (	
					ResultSet rs= pstat.executeQuery();
					){
				 while(rs.next()) {
					 int board_seq = rs.getInt("board_seq");
					 String title = rs.getString("title");
					 String content = rs.getString("content");
					 Timestamp write_date = rs.getTimestamp("write_date");
					 int view_count = rs.getInt("view_count");
					 int thumbs_up = rs.getInt("thumbs_up");
					 String delete_yn = rs.getString("delete_yn");
					 Timestamp delete_date = rs.getTimestamp("delete_date");
					 int game_id = rs.getInt("game_id");
					 String nickname = rs.getString("nickname");
					 list.add(new BoardDTO(board_seq,title,content,write_date,view_count
							 ,thumbs_up,delete_yn,delete_date,game_id,nickname));
				 }
				 return list;
			}
		}
 	}
	
	// 전체 게시글 카운트 조회 
	public int getRecordCount() throws Exception {
		String sql = "select count(*) from board";
		try (
						Connection con = this.getconnection();
						PreparedStatement pstat = con.prepareStatement(sql);
				ResultSet rs= pstat.executeQuery();
						) {
				 rs.next();
				 return rs.getInt(1);
			}
	}
	
	
	public int searchBoardCount(String id) throws Exception {
		// 마이페이지에서 게시물 작성 수를 확인하는 메서드
//		해당 회원이 작성한 게시물 수를 반환
		
	    String sql = "select count(*) from board where nickname = (select nickname from member where userid = ?)";
	    try (
	        Connection con = this.getconnection();  
	        PreparedStatement pstat = con.prepareStatement(sql);
	    ) {
	        pstat.setString(1, id);

	        try (ResultSet rs = pstat.executeQuery()) {
	            if (rs.next()) {
	                return rs.getInt(1); 
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new Exception("게시물 작성 수 조회 중 오류 발생", e);
	    }
	    return 0;  // 기본값으로 0을 반환
	}
	
	

	public List<BoardDTO> searchMyBoardList(int startNum, int endNum,String id) throws Exception{
//		'해당하는 회원이' 작성한 글 리스트를 반환하는 메서드
//		회원의 id를 인자로 가져와서 닉네임을 찾는다.
		
	System.out.println("board_seq");
	String sql = "select * from (select board.*, row_number() over(order by board_seq desc) rown from board where nickname = (select nickname from member where userid = ?)) where rown between ? and ?";
	try (
			Connection con = this.getconnection();
			PreparedStatement pstat = con.prepareStatement(sql);	
			) {
		List<BoardDTO> list = new ArrayList<>();
		pstat.setString(1,id);
		pstat.setInt(2, startNum);
		pstat.setInt(3, endNum);
		
		try (	
				ResultSet rs= pstat.executeQuery();
				){
			 while(rs.next()) {
				 int board_seq = rs.getInt("board_seq");
				 String title = rs.getString("title");
				 String content = rs.getString("content");
				 Timestamp write_date = rs.getTimestamp("write_date");
				 int view_count = rs.getInt("view_count");
				 int thumbs_up = rs.getInt("thumbs_up");
				 String delete_yn = rs.getString("delete_yn");
				 Timestamp delete_date = rs.getTimestamp("delete_date");
				 int game_id = rs.getInt("game_id");
				 String nickname = rs.getString("nickname");
				 list.add(new BoardDTO(board_seq,title,content,write_date,view_count
						 ,thumbs_up,delete_yn,delete_date,game_id,nickname));
			 }
			 return list;
		}
	}
		}
	
	
	
	
	public List<BoardDTO> searchMyCommentedBoardList(int startNum, int endNum, String id) throws Exception {
		//해당 회원이 댓글을 작성한 게시물들의 리스트를 중복없이 반환함
		
    System.out.println("board_seq");
    String sql = "select * from (select board.*, row_number() over(order by board_seq desc) rown " +
                 "from board where board_seq in (select distinct board_seq from reply where nickname = (select nickname from member where userid = ?))) " +
                 "where rown between ? and ?";
    try (
            Connection con = this.getconnection();
            PreparedStatement pstat = con.prepareStatement(sql);    
            ) {
        List<BoardDTO> list = new ArrayList<>();
        pstat.setString(1, id);
        pstat.setInt(2, startNum);
        pstat.setInt(3, endNum);
        
        try (
                ResultSet rs = pstat.executeQuery();
                ) {
            while (rs.next()) {
                int board_seq = rs.getInt("board_seq");
                String title = rs.getString("title");
                String content = rs.getString("content");
                Timestamp write_date = rs.getTimestamp("write_date");
                int view_count = rs.getInt("view_count");
                int thumbs_up = rs.getInt("thumbs_up");
                String delete_yn = rs.getString("delete_yn");
                Timestamp delete_date = rs.getTimestamp("delete_date");
                int game_id = rs.getInt("game_id");
                String nickname = rs.getString("nickname");
                list.add(new BoardDTO(board_seq, title, content, write_date, view_count, thumbs_up, delete_yn, delete_date, game_id, nickname));
            }
            return list;
        }
    }
}



	public List<BoardDTO> searchMyBookmarkedBoardList(int startNum, int endNum, String id) throws Exception {
//		회원이 북마크한 게시글 리스트를 반환하는 메서드
		
	    System.out.println("board_seq");
	    String sql = "select * from (select board.*, row_number() over(order by board_seq desc) rown " +
	                 "from board where board_seq in (select board_seq from bookmark where userid = ?)) " +
	                 "where rown between ? and ?";
	    try (
	            Connection con = this.getconnection();
	            PreparedStatement pstat = con.prepareStatement(sql);    
	            ) {
	        List<BoardDTO> list = new ArrayList<>();
	        pstat.setString(1, id);
	        pstat.setInt(2, startNum);
	        pstat.setInt(3, endNum);
	        
	        try (
	                ResultSet rs = pstat.executeQuery();
	                ) {
	            while (rs.next()) {
	                int board_seq = rs.getInt("board_seq");
	                String title = rs.getString("title");
	                String content = rs.getString("content");
	                Timestamp write_date = rs.getTimestamp("write_date");
	                int view_count = rs.getInt("view_count");
	                int thumbs_up = rs.getInt("thumbs_up");
	                String delete_yn = rs.getString("delete_yn");
	                Timestamp delete_date = rs.getTimestamp("delete_date");
	                int game_id = rs.getInt("game_id");
	                String nickname = rs.getString("nickname");
	                list.add(new BoardDTO(board_seq, title, content, write_date, view_count, thumbs_up, delete_yn, delete_date, game_id, nickname));
	            }
	            return list;
	        }
	    }
	}

	
	
	
	
	
	
	//더미데이터만들기
	public static void main(String[] args) throws Exception {
        String url = "jdbc:oracle:thin:@localhost:1521:xe";
        String id = "bunny";
        String pw = "bunny";

        // SQL 문: 더미 데이터를 삽입하는 SQL 문
        String sql = "INSERT INTO Board (BOARD_SEQ, TITLE, CONTENT, WRITE_DATE, VIEW_COUNT, THUMBS_UP, DELETE_YN, GAME_ID, NICKNAME) " +
                     "VALUES (board_seq.NEXTVAL, ?, ?, SYSDATE, ?, ?, 'N', ?, ?)";

        try (Connection con = DriverManager.getConnection(url, id, pw);
             PreparedStatement pstat = con.prepareStatement(sql)) {
            for (int i = 1; i <= 50; i++) {
                pstat.setString(1, "Title " + i); // TITLE
                pstat.setString(2, "Content " + i); // CONTENT
                pstat.setInt(3, (int) (Math.random() * 100)); // VIEW_COUNT
                pstat.setInt(4, (int) (Math.random() * 50)); // THUMBS_UP
                pstat.setInt(5, (int) (Math.random() * 10) + 1); // GAME_ID
                pstat.setString(6, "User" + i); // NICKNAME
                pstat.addBatch();
            }
            pstat.executeBatch();
            System.out.println("50개의 더미 데이터가 성공적으로 삽입되었습니다.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	
	
}
