<|editable_region_start|>
import numpy as np
from math import sin, cos, sqrt, pi

def truss(A):
    """Computes mass and stress for the 10-bar truss problem

    Parameters
    ----------
    A : ndarray of length nbar
        cross-sectional areas of each bar
        see image in book for number order if needed

    Outputs
    -------
    mass : float
        mass of the entire structure
    stress : ndarray of length nbar
        stress in each bar

    """

    # --- specific truss setup -----
    P = 1e5  # applied loads
    Ls = 360.0  # length of sides
    Ld = sqrt(360**2 * 2)  # length of diagonals

    start = [5, 3, 6, 4, 4, 2, 5, 6, 3, 4, 3]
    finish = [3, 1, 4, 2, 3, 1, 4, 3, 2, 1, 4]
    phi = np.array([0, 0, 0, 0, 90, 90, -45, 45, -45, 45, 90])*pi/180
    L = np.array([Ls, Ls, Ls, Ls, Ls, Ls, Ld, Ld, Ld, Ld, Ls])

    nbar = len(A)  # number of bars
    E = 1e7*np.ones(nbar)  # modulus of elasticity
    rho = 0.1*np.ones(nbar)  # material density

    Fx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    Fy = np.array([0.0, -P, 0.0, -P, 0.0, 0.0])
    dFx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    dFy = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    rigid = [False, False, False, False, True, True]
    # ------------------

    n = len(Fx)  # number of nodes
    DOF = 2  # number of degrees of freedom

    # mass
    mass = np.sum(rho*A*L)

    # stiffness and stress matrices
    K = np.zeros((DOF*n, DOF*n))
    S = np.zeros((nbar, DOF*n))

    for i in range(nbar):  # loop through each bar

        # compute submatrix for each element
        Ksub, Ssub = bar(E[i], A[i], L[i], phi[i])

        # insert submatrix into global matrix
        idx = node2idx([start[i], finish[i]], DOF)  # pass in the starting and ending node number for this element
        S[i, idx] = Ssub
        
      K[np.ix_(idx, idx)] += Ksub

    # applied loads
    F = np.zeros((n*DOF, 1))

    FX = 0
    FY = 0

    for i in range(n):
        idx = node2idx([i+1], DOF)  # add 1 b.c. made indexing 1-based for convenience
        F[idx[0]] = Fx[i]
        F[idx[1]] = Fy[i]
    dFx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    dFy = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    rigid = [False, False, False, False, True, True]
    # ------------------

    n = len(Fx)  # number of nodes
    DOF = 2  # number of degrees of freedom

    # mass
    mass = np.sum(rho*A*L)

    # stiffness and stress matrices
    K = np.zeros((DOF*n, DOF*n))
    S = np.zeros((nbar, DOF*n))

    for i in range(nbar):  # loop through each bar

        # compute submatrix for each element
        Ksub, Ssub = bar(E[i], A[i], L[i], phi[i])

        # insert submatrix into global matrix
        idx = node2idx([start[i], finish[i]], DOF)  # pass in the starting and ending node number for this element
        S[i, idx] = Ssub
        
      K[np.ix_(idx, idx)] += Ksub

    # applied loads
    F = np.zeros((n*DOF, 1))

    FX = 0
    FY = 0

    for i in range(n):
        idx = node2idx([i+1], DOF)  # add 1 b.c. made indexing 1-based for convenience
        F[idx[0]] = Fx[i]
        F[idx[1]] = Fy[i]
    dFx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    dFy = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    rigid = [False, False, False, False, True, True]
    # ------------------

    n = len(Fx)  # number of nodes
    DOF = 2  # number of degrees of freedom

    # mass
    mass = np.sum(rho*A*L)

    # stiffness and stress matrices
    K = np.zeros((DOF*n, DOF*n))
    S = np.zeros((nbar, DOF*n))

    for i in range(nbar):  # loop through each bar

        # compute submatrix for each element
        Ksub, Ssub = bar(E[i], A[i], L[i], phi[i])

        # insert submatrix into global matrix
        idx = node2idx([start[i], finish[i]], DOF)  # pass in the starting and ending node number for this element
        S[i, idx] = Ssub
        
      K[np.ix_(idx, idx)] += Ksub

    # applied loads
    F = np.zeros((n*DOF, 1))

    FX = 0
    FY = 0

    for i in range(n):
        idx = node2idx([i+1], DOF)  # add 1 b.c. made indexing 1-based for convenience
        F[idx[0]] = Fx[i]
        F[idx[1]] = Fy[i]
    dFx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    dFy = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    rigid = [False, False, False, False, True, True]
    # ------------------

    n = len(Fx)  # number of nodes
    DOF = 2  # number of degrees of freedom

    # mass
    mass = np.sum(rho*A*L)

    # stiffness and stress matrices
    K = np.zeros((DOF*n, DOF*n))
    S = np.zeros((nbar, DOF*n))

    for i in range(nbar):  # loop through each bar

        # compute submatrix for each element
        Ksub, Ssub = bar(E[i], A[i], L[i], phi[i])

        # insert submatrix into global matrix
        idx = node2idx([start[i], finish[i]], DOF)  # pass in the starting and ending node number for this element
        S[i, idx] = Ssub
        
      K[np.ix_(idx, idx)] += Ksub

    # applied loads
    F = np.zeros((n*DOF, 1))

    FX = 0
    FY = 0

    for i in range(n):
        idx = node2idx([i+1], DOF)  # add 1 b.c. made indexing 1-based for convenience
        F[idx[0]] = Fx[i]
        F[idx[1]] = Fy[i]
    dFx = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    dFy = np.array([0.0, 0.0, 0.0, 0.0, 0.0, 0.0])
    rigid = [False, False, False, False, True, True]
    # ------------------

    n = len(Fx)  # number of nodes
    DOF = 2  # number of degrees of freedom

    # mass
    mass = np.sum(rho*A*L)

    # stiffness and stress matrices
    K =