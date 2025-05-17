package com.assetsservice.util;

import com.assetsservice.model.db.Asset;
import com.assetsservice.model.db.AssetAssignment;
import com.assetsservice.model.db.AssetPhoto;
import com.assetsservice.model.enumtype.AssetStatus;
import com.assetsservice.model.enumtype.AssetType;
import com.assetsservice.model.enumtype.AssignmentStatus;
import com.assetsservice.repository.AssetAssignmentRepository;
import com.assetsservice.repository.AssetPhotoRepository;
import com.assetsservice.repository.AssetRepository;
import org.springframework.http.MediaType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Helper class for creating test entities and data for integration tests.
 */
public class TestHelper {


}
