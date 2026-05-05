// Redirected to HomePage
const HomePage = () => import('../views/Website/HomePage.vue')
const TheoDoiDonHangPage = () => import('../views/Website/TheoDoiDonHangPage.vue')
const CustomerProfilePage = () => import('../views/Website/CustomerProfilePage.vue')
const FavoritePage = () => import('../views/Website/FavoritePage.vue')
const ComparePage = () => import('../views/Website/ComparePage.vue')

const customerRoutes = [
  {
    path: '/customer',
    redirect: '/' // Redirect to new homepage
  },
  {
    path: '/theo-doi-don-hang',
    name: 'theo-doi-don-hang',
    component: TheoDoiDonHangPage,
    meta: { public: true }
  },
  {
    path: '/tai-khoan',
    name: 'customer-profile',
    component: CustomerProfilePage,
    meta: { public: true }
  },
  {
    path: '/tai-khoan/thong-tin',
    name: 'customer-info',
    component: CustomerProfilePage,
    meta: { public: true }
  },
  {
    path: '/tai-khoan/yeu-thich',
    name: 'favorite',
    component: FavoritePage,
    meta: { public: true }
  },
  {
    path: '/so-sanh',
    name: 'compare',
    component: ComparePage,
    meta: { public: true }
  }
]

export default customerRoutes
