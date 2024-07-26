export default function Navigation() {
    return (
        <nav className="bg-white border-gray-200 dark:bg-gray-900">
          <div className="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">
            <p className="flex items-center space-x-3 rtl:space-x-reverse">
               <svg width="60" height="60" viewBox="0 0 258 245" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M183 2.5H75C34.9594 2.5 2.5 34.9594 2.5 75V170C2.5 210.041 34.9594 242.5 75 242.5H183C223.041 242.5 255.5 210.041 255.5 170V75C255.5 34.9594 223.041 2.5 183 2.5Z" fill="#1E40AF"/>
                    <path d="M183 2.5H75C34.9594 2.5 2.5 34.9594 2.5 75V170C2.5 210.041 34.9594 242.5 75 242.5H183C223.041 242.5 255.5 210.041 255.5 170V75C255.5 34.9594 223.041 2.5 183 2.5Z" stroke="black" strokeWidth="5"/>
                    <path d="M122.545 109.636H99.8182C99.5152 107.303 98.8939 105.197 97.9545 103.318C97.0152 101.439 95.7727 99.8333 94.2273 98.5C92.6818 97.1667 90.8485 96.1515 88.7273 95.4545C86.6364 94.7273 84.3182 94.3636 81.7727 94.3636C77.2576 94.3636 73.3636 95.4697 70.0909 97.6818C66.8485 99.8939 64.3485 103.091 62.5909 107.273C60.8636 111.455 60 116.515 60 122.455C60 128.636 60.8788 133.818 62.6364 138C64.4242 142.152 66.9242 145.288 70.1364 147.409C73.3788 149.5 77.2121 150.545 81.6364 150.545C84.1212 150.545 86.3788 150.227 88.4091 149.591C90.4697 148.955 92.2727 148.03 93.8182 146.818C95.3939 145.576 96.6818 144.076 97.6818 142.318C98.7121 140.53 99.4242 138.515 99.8182 136.273L122.545 136.409C122.152 140.53 120.955 144.591 118.955 148.591C116.985 152.591 114.273 156.242 110.818 159.545C107.364 162.818 103.152 165.424 98.1818 167.364C93.2424 169.303 87.5758 170.273 81.1818 170.273C72.7576 170.273 65.2121 168.424 58.5455 164.727C51.9091 161 46.6667 155.576 42.8182 148.455C38.9697 141.333 37.0455 132.667 37.0455 122.455C37.0455 112.212 39 103.53 42.9091 96.4091C46.8182 89.2879 52.1061 83.8788 58.7727 80.1818C65.4394 76.4848 72.9091 74.6364 81.1818 74.6364C86.8182 74.6364 92.0303 75.4242 96.8182 77C101.606 78.5455 105.818 80.8182 109.455 83.8182C113.091 86.7879 116.045 90.4394 118.318 94.7727C120.591 99.1061 122 104.061 122.545 109.636Z" fill="white"/>
                    <path d="M220.545 109.636H197.818C197.515 107.303 196.894 105.197 195.955 103.318C195.015 101.439 193.773 99.8333 192.227 98.5C190.682 97.1667 188.848 96.1515 186.727 95.4545C184.636 94.7273 182.318 94.3636 179.773 94.3636C175.258 94.3636 171.364 95.4697 168.091 97.6818C164.848 99.8939 162.348 103.091 160.591 107.273C158.864 111.455 158 116.515 158 122.455C158 128.636 158.879 133.818 160.636 138C162.424 142.152 164.924 145.288 168.136 147.409C171.379 149.5 175.212 150.545 179.636 150.545C182.121 150.545 184.379 150.227 186.409 149.591C188.47 148.955 190.273 148.03 191.818 146.818C193.394 145.576 194.682 144.076 195.682 142.318C196.712 140.53 197.424 138.515 197.818 136.273L220.545 136.409C220.152 140.53 218.955 144.591 216.955 148.591C214.985 152.591 212.273 156.242 208.818 159.545C205.364 162.818 201.152 165.424 196.182 167.364C191.242 169.303 185.576 170.273 179.182 170.273C170.758 170.273 163.212 168.424 156.545 164.727C149.909 161 144.667 155.576 140.818 148.455C136.97 141.333 135.045 132.667 135.045 122.455C135.045 112.212 137 103.53 140.909 96.4091C144.818 89.2879 150.106 83.8788 156.773 80.1818C163.439 76.4848 170.909 74.6364 179.182 74.6364C184.818 74.6364 190.03 75.4242 194.818 77C199.606 78.5455 203.818 80.8182 207.455 83.8182C211.091 86.7879 214.045 90.4394 216.318 94.7727C218.591 99.1061 220 104.061 220.545 109.636Z" fill="white"/>
                </svg>
                <span className="self-center text-2xl font-semibold whitespace-nowrap dark:text-white">Calory Counter</span>
            </p>
            <button data-collapse-toggle="navbar-default" type="button" className="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-gray-500 rounded-lg md:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200 dark:text-gray-400 dark:hover:bg-gray-700 dark:focus:ring-gray-600" aria-controls="navbar-default" aria-expanded="false">
                <span className="sr-only">Open main menu</span>
                <svg className="w-5 h-5" aria-hidden="true" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 17 14">
                    <path stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M1 1h15M1 7h15M1 13h15"/>
                </svg>
            </button>
            <div className="hidden w-full md:block md:w-auto" id="navbar-default">
              <ul className="font-medium flex flex-col p-4 md:p-0 mt-4 border border-gray-100 rounded-lg bg-gray-50 md:flex-row md:space-x-8 rtl:space-x-reverse md:mt-0 md:border-0 md:bg-white dark:bg-gray-800 md:dark:bg-gray-900 dark:border-gray-700">
                <li>
                    <a href="/" className="block py-2 px-3 text-white bg-blue-700 rounded md:bg-transparent md:text-blue-700 md:p-0 dark:text-white md:dark:text-blue-500" aria-current="page">
                        Dashboard
                    </a>
                </li>
                <li>
                    <a href="/food" className="block py-2 px-3 text-gray-900 rounded hover:bg-gray-100 md:hover:bg-transparent md:border-0 md:hover:text-blue-700 md:p-0 dark:text-white md:dark:hover:text-blue-500 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:bg-transparent">
                        Food
                    </a>
                </li>
                <li>
                    <a href="/recipe" className="block py-2 px-3 text-gray-900 rounded hover:bg-gray-100 md:hover:bg-transparent md:border-0 md:hover:text-blue-700 md:p-0 dark:text-white md:dark:hover:text-blue-500 dark:hover:bg-gray-700 dark:hover:text-white md:dark:hover:bg-transparent">
                        Recipe
                    </a>
                </li>
              </ul>
            </div>
          </div>
        </nav>
    );
}