package ru.job4j.site.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.site.SiteSrv;
import ru.job4j.site.domain.Breadcrumb;
import ru.job4j.site.dto.CategoryDTO;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.dto.TopicDTO;
import ru.job4j.site.service.*;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


/**
 * CheckDev пробное собеседование
 * IndexControllerTest тесты на контроллер IndexController
 *
 * @author Dmitry Stepanov
 * @version 24.09.2023 21:50
 */
@SpringBootTest(classes = SiteSrv.class)
@AutoConfigureMockMvc
class IndexControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CategoriesService categoriesService;
    @MockBean
    private TopicsService topicsService;
    @MockBean
    private InterviewsService interviewsService;
    @MockBean
    private AuthService authService;
    @MockBean
    private NotificationService notificationService;
    @MockBean
    private ProfilesService profilesService;

    private IndexController indexController;

    @BeforeEach
    void initTest() {
        this.indexController = new IndexController(
                categoriesService, interviewsService, authService, notificationService, profilesService
        );
    }

    @Test
    void whenGetIndexPageThenReturnIndex() throws Exception {
        this.mockMvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void whenGetIndexPageExpectModelAttributeThenOk() throws JsonProcessingException {
        var topicDTO1 = new TopicDTO();
        topicDTO1.setId(1);
        topicDTO1.setName("topic1");
        var topicDTO2 = new TopicDTO();
        topicDTO2.setId(2);
        topicDTO2.setName("topic2");
        var cat1 = new CategoryDTO(1, "name1");
        var cat2 = new CategoryDTO(2, "name2");
        var listCat = List.of(cat1, cat2);
        var firstInterview = new InterviewDTO(1, 1, 1, 1,
                "interview1", "description1", "contact1",
                "30.02.2024", "09.10.2023", 1);
        var secondInterview = new InterviewDTO(2, 1, 1, 2,
                "interview2", "description2", "contact2",
                "30.02.2024", "09.10.2023", 1);
        var firstProfile = new ProfileDTO(1, "Name1", "middle", 1,
                new GregorianCalendar(2024, Calendar.MARCH, 30),
                new GregorianCalendar(2023, Calendar.NOVEMBER, 9));
        var secondProfile = new ProfileDTO(2, "Name2", "junior", 2,
                new GregorianCalendar(2024, Calendar.FEBRUARY, 22),
                new GregorianCalendar(2023, Calendar.OCTOBER, 12));
        Map<Integer, Optional<ProfileDTO>> mapOptionalProfiles = Map.of(
                1, Optional.of(firstProfile),
                2, Optional.of(secondProfile)
        );
        var listInterviews = List.of(firstInterview, secondInterview);
        when(topicsService.getByCategory(cat1.getId())).thenReturn(List.of(topicDTO1));
        when(topicsService.getByCategory(cat2.getId())).thenReturn(List.of(topicDTO2));
        when(categoriesService.getMostPopular()).thenReturn(listCat);
        when(interviewsService.getByType(1)).thenReturn(listInterviews);
        when(profilesService.getProfileById(firstProfile.getId())).thenReturn(Optional.of(firstProfile));
        when(profilesService.getProfileById(secondProfile.getId())).thenReturn(Optional.of(secondProfile));
        var listBread = List.of(new Breadcrumb("Главная", "/"));
        var model = new ConcurrentModel();
        var view = indexController.getIndexPage(model, null);
        var actualCategories = model.getAttribute("categories");
        var actualBreadCrumbs = model.getAttribute("breadcrumbs");
        var actualUserInfo = model.getAttribute("userInfo");
        var actualInterviews = model.getAttribute("new_interviews");
        var actualProfiles = model.getAttribute("profiles");

        assertThat(view).isEqualTo("index");
        assertThat(actualCategories).usingRecursiveComparison().isEqualTo(listCat);
        assertThat(actualBreadCrumbs).usingRecursiveComparison().isEqualTo(listBread);
        assertThat(actualUserInfo).isNull();
        assertThat(actualInterviews).usingRecursiveComparison().isEqualTo(listInterviews);
        assertThat(actualProfiles).usingRecursiveComparison().isEqualTo(mapOptionalProfiles);
    }
}