```<|start_of_file|>
<|editable_region_start|>
@Service
public class MovieLicenseService {
    private final MovieLicenseMapper mapper;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public MovieLicenseService(MovieLicenseMapper mapper) {
        this.mapper = mapper;
    }

    public List<MovieLicenseDTO> getMovieLicenses(String sortCol, String sortOrder, int limit, int offset) {
        List<MovieLicenseDTO> movies = mapper.getMovieLicenses(sortCol, sortOrder, limit, offset);
        
        return movies.stream()
            .map(this::calculateCurrentStatus)
            .collect(Collectors.toList());
    }

    private MovieLicenseDTO calculateCurrentStatus(MovieLicenseDTO movie) {
        String status = calculateStatus(movie.getCurrentReleaseDate(), movie.getCurrentExpiryDate());
        movie.setCurrentStatus(status);
        return movie;
    }

    @Cacheable <|user_cursor_is_here|>
    private String calculateStatus(String releaseDateStr, String expiryDateStr) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime releaseDate = LocalDateTime.parse(releaseDateStr, DATE_FORMATTER);
        LocalDateTime expiryDate = LocalDateTime.parse(expiryDateStr, DATE_FORMATTER);

        if (now.isBefore(releaseDate)) {
            return "PENDING";
        } else if (now.isAfter(expiryDate)) {
            return "EXPIRED";
        } else {
            return "ACTIVE";
        }
    }

    @Transactional
    public void logLicenseHistory(LicenseHistoryRequest request) {
        LicenseHistoryEntry entry = LicenseHistoryEntry.builder()
            .movieId(request.getMovieId())
            .releaseDate(request.getOldReleaseDate())
            .expiryDate(request.getOldExpiryDate())
            .status(request.getOldStatus())
            .changeDate(LocalDateTime.now())
            .build();

        mapper.insertLicenseHistory(entry);
    }
}
<|editable_region_end|>
```