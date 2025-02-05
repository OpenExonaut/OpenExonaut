package xyz.openexonaut.extension.exolib;

import org.lwjgl.system.*;

import physx.*;
import physx.character.*;
import physx.common.*;
import physx.cooking.*;
import physx.geometry.*;
import physx.physics.*;

public class ExoPhysics {
    private static final int PX_PHYSICS_VERSION = PxTopLevelFunctions.getPHYSICS_VERSION();
    private final PxFoundation foundation;
    private final PxPhysics physics;
    private final PxCookingParams cookingParams;
    private final PxCpuDispatcher defaultDispatcher;
    private final PxMaterial defaultMaterial;
    private final PxFilterData defaultFilterData;

    private final PxSceneDesc sceneDesc;
    private final PxVec3 nullVector;
    private final PxTransform identityPose;
    private final PxCapsuleControllerDesc controllerDesc;

    public ExoPhysics() {
        foundation = PxTopLevelFunctions.CreateFoundation(PX_PHYSICS_VERSION, new PxDefaultAllocator(), new PxDefaultErrorCallback());
        PxTolerancesScale tolerances = new PxTolerancesScale();
        physics = PxTopLevelFunctions.CreatePhysics(PX_PHYSICS_VERSION, foundation, tolerances);
        cookingParams = new PxCookingParams(tolerances);
        defaultDispatcher = PxTopLevelFunctions.DefaultCpuDispatcherCreate(2);
        defaultMaterial = physics.createMaterial(0.5f, 0.5f, 0.5f);
        defaultFilterData = new PxFilterData(1, 0xffffffff, 0, 0);
        PxTopLevelFunctions.InitExtensions(physics);

        sceneDesc = new PxSceneDesc(physics.getTolerancesScale());
        sceneDesc.setCpuDispatcher(defaultDispatcher);
        sceneDesc.setFilterShader(PxTopLevelFunctions.DefaultFilterShader());

        nullVector = new PxVec3(0f, 0f, 0f);
        identityPose = new PxTransform(PxIDENTITYEnum.PxIdentity);
        identityPose.setP(nullVector);

        controllerDesc = new PxCapsuleControllerDesc();
        controllerDesc.setMaterial(defaultMaterial);
        controllerDesc.setHeight(10f);
        controllerDesc.setRadius(1.5f);

        try (MemoryStack mem = MemoryStack.stackPush()) {
            sceneDesc.setGravity(PxVec3.createAt(mem, MemoryStack::nmalloc, 0f, -100f, 0f));
            cookingParams.setMeshPreprocessParams(PxMeshPreprocessingFlags.createAt(mem, MemoryStack::nmalloc, PxMeshPreprocessingFlagEnum.eDISABLE_CLEAN_MESH.value));
        }
    }

    public PxScene createEmptyScene() {
        return physics.createScene(sceneDesc);
    }

    public PxTriangleMeshGeometry cookTriangleMesh(PxTriangleMeshDesc meshDesc) {
        return new PxTriangleMeshGeometry(PxTopLevelFunctions.CreateTriangleMesh(cookingParams, meshDesc));
    }

    public PxRigidStatic createStaticBody(PxGeometry geometry) {
        PxShape shape = physics.createShape(geometry, defaultMaterial, true);
        shape.setSimulationFilterData(defaultFilterData);

        PxRigidStatic body = physics.createRigidStatic(identityPose);
        body.attachShape(shape);
        return body;
    }

    public PxController createController(PxControllerManager controllerManager) {
        return controllerManager.createController(controllerDesc);
    }
}
